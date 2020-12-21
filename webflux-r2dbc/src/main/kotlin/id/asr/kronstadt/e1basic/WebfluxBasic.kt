package id.asr.kronstadt.e1basic

import kotlinx.coroutines.reactive.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.slf4j.MDCContext
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
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

data class Sleep(
		@Id
		@Column("id")
		var id: Long = 0,
)

@Repository
interface SleepRepository: CoroutineCrudRepository<Sleep, Long> {
	@Query(value = "SELECT SLEEP(0.1) AS result")
	suspend fun sleep(): Int
}

@Component
class ContextFilter: WebFilter {
	override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
		MDC.put("starting-thread", Thread.currentThread().name)
		return mono(MDCContext()) {
			chain.filter(exchange).awaitSingleOrNull()
		}
	}
}

@Controller
class RootController(
		private val sleepRepository: SleepRepository
) {
	/// Note: r2dbc seems to always run in a single thread.
	// 2020-12-21 15:59:29.958  INFO 13610 --- [atcher-worker-1] id.asr.kronstadt.e1basic.RootController  : source: DefaultDispatcher-worker-1 start sleeping
	// 2020-12-21 15:59:30.376 DEBUG 13610 --- [actor-tcp-nio-1] o.s.r2dbc.core.DefaultDatabaseClient     : Executing SQL statement [SELECT SLEEP(1) AS result]
	// 2020-12-21 15:59:31.402  INFO 13610 --- [actor-tcp-nio-1] id.asr.kronstadt.e1basic.RootController  : source: null start counting
	// 2020-12-21 15:59:31.430 DEBUG 13610 --- [actor-tcp-nio-1] o.s.r2dbc.core.DefaultDatabaseClient     : Executing SQL statement [SELECT COUNT(sleep.id) FROM sleep]
	// 2020-12-21 15:59:31.458  INFO 13610 --- [actor-tcp-nio-1] id.asr.kronstadt.e1basic.RootController  : source: null done
	@GetMapping("/")
	suspend fun get(): ResponseEntity<String> {
		log.info("source: ${MDC.get("starting-thread")} start sleeping")
		sleepRepository.sleep()
		log.info("source: ${MDC.get("starting-thread")} start counting")
		val count = sleepRepository.count()
		log.info("source: ${MDC.get("starting-thread")} done")
		return ResponseEntity.ok("count is $count\n")
	}

	companion object {
		val log = LoggerFactory.getLogger(RootController::class.java)
	}
}

@EnableWebFlux
@EnableR2dbcRepositories
@SpringBootApplication
class Application

//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    3   1.4      3       6
//Processing:   744 2021 118.1   2032    2126
//Waiting:      737 2021 118.6   2032    2126
//Total:        744 2024 118.0   2036    2127
//
//Percentage of the requests served within a certain time (ms)
//  50%   2036
//  66%   2051
//  75%   2060
//  80%   2064
//  90%   2071
//  95%   2074
//  98%   2089
//  99%   2089
// 100%   2127 (longest request)
fun main(args: Array<String>) {
	System.setProperty("reactor.netty.ioWorkerCount", "1");

	val app = SpringApplication(Application::class.java)
	app.run()
}
