package id.asr.kronstadt.e4suspenddispatcher

import kotlinx.coroutines.*
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
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.future.await as awaitUnconfined

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

object CustomDispatcher {
    @OptIn(ObsoleteCoroutinesApi::class)
    val dispatcher = newSingleThreadContext("await")
}

suspend fun <T> CompletionStage<T>.await(): T =
        withContext(Dispatchers.IO) {
            awaitUnconfined()
        }

@Controller
class RootController(
        private val sleepRepository: SleepRepository
) {
    @GetMapping("/")
    suspend fun get(): ResponseEntity<String> {
//        return withContext(Dispatchers.Default) {
            log.info("context: ${MDC.get("kotlin")}")
            println(coroutineContext)

            log.info("start sleeping")
            sleepRepository.sleep().await()
            log.info("start counting")
            val count = sleepRepository.count().await()
            log.info("done")

            return ResponseEntity.ok("count is $count\n")
//        }
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

fun main(args: Array<String>) {
    System.setProperty("reactor.netty.ioWorkerCount", "1");

    val app = SpringApplication(Application::class.java)
    app.run()
}
