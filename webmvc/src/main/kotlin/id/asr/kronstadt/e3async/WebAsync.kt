package id.asr.kronstadt.e3async

import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
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
    @Query(value = "SELECT SLEEP(2) AS result", nativeQuery = true)
    fun sleep(): CompletableFuture<Int>

    fun count(): CompletableFuture<Long>
}

@Controller
class RootController(
        private val sleepRepository: SleepRepository
) {
    @GetMapping("/")
    fun get(): ResponseEntity<String> = runBlocking {
        log.info("start sleeping")
        sleepRepository.sleep().await()
        log.info("start counting")
        val count =  sleepRepository.count().await()
        log.info("done")
        ResponseEntity.ok("count is $count\n")
    }

    companion object {
        val log = LoggerFactory.getLogger(RootController::class.java)
    }
}

@EnableAsync
@EnableJpaRepositories
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    System.setProperty("server.tomcat.threads.min-spare", "1")
    System.setProperty("server.tomcat.threads.max", "1")

    val app = SpringApplication(Application::class.java)
    app.run()
}
