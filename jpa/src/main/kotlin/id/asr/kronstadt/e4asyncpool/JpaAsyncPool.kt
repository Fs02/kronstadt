package id.asr.kronstadt.e4asyncpool

import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Repository
import java.net.URI
import java.util.concurrent.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis


@Entity
data class Sleep(
        @Id
        @Column(name = "id", nullable = false)
        var id: Long = 0,
)

@Async("repositoryThreadPool")
@Repository
interface SleepRepository: org.springframework.data.repository.Repository<Sleep, Long> {
    @Query(value = "SELECT SLEEP(2) AS result", nativeQuery = true)
    fun sleep(): CompletableFuture<Int>

    fun count(): CompletableFuture<Long>
}

@Configuration
@EnableAsync
class ThreadPoolConfig: AsyncConfigurer {
    @Bean(name = ["repositoryThreadPool"])
    fun repositoryThreadPool(): Executor? {
        return newThreadPool(1, 0, "repository")
    }

    override fun getAsyncExecutor(): Executor? {
        return newThreadPool(1, 0, "default")
    }

    private fun newThreadPool(poolSize: Int, keepAliveMills: Long, workerNamePrefix: String): ThreadPoolExecutor {
        return ThreadPoolExecutor(
                poolSize, poolSize, keepAliveMills, TimeUnit.MILLISECONDS, LinkedBlockingQueue()
        ) { pool ->
            val worker = Executors.defaultThreadFactory().newThread(pool)
            worker.name = "$workerNamePrefix-${worker.id}"
            worker
        }
    }
}

@EnableJpaRepositories
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    val log = LoggerFactory.getLogger(Application::class.java)
    val app = SpringApplication(Application::class.java)
    app.webApplicationType = WebApplicationType.NONE

    URI("")

    val context = app.run()
    val repo = context.getBean(SleepRepository::class.java)

    val time = measureTimeMillis {
        runBlocking {
            repeat(10) {
                launch {
                    log.info("start number: $it")
                    repo.count().await()
                    val sleep = repo.sleep().await()
                    log.info("result number: $it is $sleep")
                    log.info("done number: $it")
                }
            }
        }
    }

    log.info("time: ${time}ms")
    exitProcess(0)
}
