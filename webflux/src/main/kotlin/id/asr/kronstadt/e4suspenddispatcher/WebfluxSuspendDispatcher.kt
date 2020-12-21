package id.asr.kronstadt.e4suspenddispatcher

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.http.ResponseEntity
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.reactive.config.EnableWebFlux
import java.util.concurrent.CompletableFuture
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Sleep(
        @Id
        @Column(name = "id", nullable = false)
        var id: Long = 0,
)

@Async
@Repository
interface SleepRepository: org.springframework.data.repository.Repository<Sleep, Long> {
    @Query(value = "SELECT SLEEP(0.1) AS result", nativeQuery = true)
    fun sleep(): CompletableFuture<Int>

    fun count(): CompletableFuture<Long>
}

@Controller
class RootController(
        private val sleepRepository: SleepRepository
) {
    //2020-12-21 16:40:54.306  INFO 18240 --- [atcher-worker-2] i.a.k.e.RootController                   : start sleeping
    //2020-12-21 16:40:54.311  INFO 18240 --- [atcher-worker-2] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
    //2020-12-21 16:40:54.351 DEBUG 18240 --- [         task-1] org.hibernate.SQL                        : SELECT SLEEP(0.1) AS result
    //2020-12-21 16:40:54.540  INFO 18240 --- [atcher-worker-2] i.a.k.e.RootController                   : start counting
    //2020-12-21 16:40:54.589 DEBUG 18240 --- [         task-2] org.hibernate.SQL                        : select count(*) as col_0_0_ from sleep sleep0_
    //2020-12-21 16:40:54.592  INFO 18240 --- [atcher-worker-2] i.a.k.e.RootController                   : done
    @GetMapping("/")
    suspend fun get(): ResponseEntity<String> {
        return withContext(Dispatchers.Default) {
            log.info("context: ${MDC.get("kotlin")}")
            println(coroutineContext)

            log.info("start sleeping")
            sleepRepository.sleep().await()
            log.info("start counting")
            val count = sleepRepository.count().await()
            log.info("done")

            ResponseEntity.ok("count is $count\n")
        }
    }

    companion object {
        val log = LoggerFactory.getLogger(RootController::class.java)
    }
}

@EnableAsync
@EnableWebFlux
@EnableJpaRepositories
@SpringBootApplication
class Application

//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    4   1.5      4       6
//Processing:   193 2521 209.4   2551    2574
//Waiting:      187 2521 210.0   2550    2573
//Total:        193 2525 209.5   2554    2575
//
//Percentage of the requests served within a certain time (ms)
//  50%   2554
//  66%   2561
//  75%   2564
//  80%   2565
//  90%   2567
//  95%   2573
//  98%   2574
//  99%   2574
// 100%   2575 (longest request)
fun main(args: Array<String>) {
    System.setProperty("reactor.netty.ioWorkerCount", "1");

    val app = SpringApplication(Application::class.java)
    app.run()
}
