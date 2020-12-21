package id.asr.kronstadt.e1basic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.reactive.config.EnableWebFlux
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Sleep(
		@Id
		@Column(name = "id", nullable = false)
		var id: Long = 0,
)

@Repository
interface SleepRepository: JpaRepository<Sleep, Long> {
	@Query(value = "SELECT 1 AS result", nativeQuery = true)
	fun sleep(): Int
}

suspend fun <T> db(
		block: suspend CoroutineScope.() -> T
) = withContext(Dispatchers.IO, block)

@Controller
class RootController(
		private val sleepRepository: SleepRepository
) {
	@GetMapping("/")
	suspend fun get(): ResponseEntity<String> {
		log.info("start")
		db {
			sleepRepository.sleep()
		}

		val count = db {
			log.info("start counting")
			sleepRepository.count()
		}

		log.info("done")
		return ResponseEntity.ok("count is $count\n")
	}

	companion object {
		val log = LoggerFactory.getLogger(RootController::class.java)
	}
}

@EnableWebFlux
@EnableJpaRepositories
@SpringBootApplication
class Application

fun main(args: Array<String>) {
	System.setProperty("reactor.netty.ioWorkerCount", "1");

	val app = SpringApplication(Application::class.java)
	app.run()
}
