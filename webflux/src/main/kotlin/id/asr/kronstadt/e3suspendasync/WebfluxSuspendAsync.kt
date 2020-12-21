package id.asr.kronstadt.e3suspendasync

import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactive.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.slf4j.MDCContext
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
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
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
    @Query(value = "SELECT SLEEP(1) AS result", nativeQuery = true)
    fun sleep(): CompletableFuture<Int>

    fun count(): CompletableFuture<Long>
}

@Component
class ContextFilter: WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return mono(MDCContext()) {
            MDC.put("starting-thread", Thread.currentThread().name)
            chain.filter(exchange).awaitSingleOrNull()
        }
    }
}

@Controller
class RootController(
        private val sleepRepository: SleepRepository
) {
    @GetMapping("/")
    suspend fun get(): ResponseEntity<String> {
        log.info("source: ${MDC.get("starting-thread")} start sleeping")
        sleepRepository.sleep().await()
        log.info("source: ${MDC.get("starting-thread")} start counting")
        val count = sleepRepository.count().await()
        log.info("source: ${MDC.get("starting-thread")} done")

        return ResponseEntity.ok("count is $count\n")
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
//    System.setProperty("reactor.netty.ioWorkerCount", "1");

    val app = SpringApplication(Application::class.java)
    app.run()
}
