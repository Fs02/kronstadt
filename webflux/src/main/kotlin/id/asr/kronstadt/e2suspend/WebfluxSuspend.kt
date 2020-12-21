package id.asr.kronstadt.e2suspend

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.reactive.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
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

//@Component
//class ContextFilter: WebFilter {
//    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
//        log.info("filter ${Thread.currentThread().name}")
//
//        return mono {
//			withContext(MDCContext()) {
//				MDC.put("kotlin", "rocks")
//				chain.filter(exchange).awaitSingleOrNull()
//			}
//        }
//    }
//
//    companion object {
//        val log = LoggerFactory.getLogger(RootController::class.java)
//    }
//}

@Controller
class RootController(
		private val sleepRepository: SleepRepository
) {
	@GetMapping("/")
	suspend fun get(): ResponseEntity<String> {
		log.info("start sleeping")
		sleepRepository.sleep()
		log.info("start counting")
		val count =  sleepRepository.count()
		log.info("done")
		return ResponseEntity.ok("count is $count\n")
	}

	@GetMapping("/delay")
	suspend fun getDelay(): ResponseEntity<String> {
		log.info("start delay ${Thread.currentThread().name}")
		withContext(Dispatchers.Default) {
			delay(2000)
		}
		log.info("done ${Thread.currentThread().name}")
		return ResponseEntity.ok("finished\n")
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
