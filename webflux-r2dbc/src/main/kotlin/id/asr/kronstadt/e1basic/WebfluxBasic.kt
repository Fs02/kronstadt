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

//@Component
//class ContextFilter: WebFilter {
//	override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
//		MDC.put("starting-thread", Thread.currentThread().name)
//		return mono(MDCContext()) {
//			chain.filter(exchange).awaitSingleOrNull()
//		}
//	}
//}

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
//		log.info("source: ${MDC.get("starting-thread")} start sleeping")
		sleepRepository.sleep()
//		log.info("source: ${MDC.get("starting-thread")} start counting")
		val count = sleepRepository.count()
//		log.info("source: ${MDC.get("starting-thread")} done")
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

// Single request worker: ab -n 128 -c 128 "http://localhost:8080/"
//Concurrency Level:      128
//Time taken for tests:   2.450 seconds
//Complete requests:      128
//Failed requests:        0
//Total transferred:      11520 bytes
//HTML transferred:       1408 bytes
//Requests per second:    52.24 [#/sec] (mean)
//Time per request:       2450.008 [ms] (mean)
//Time per request:       19.141 [ms] (mean, across all concurrent requests)
//Transfer rate:          4.59 [Kbytes/sec] received
//
//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    4   1.9      4       8
//Processing:   181 2113 175.5   2123    2267
//Waiting:      174 2113 176.2   2123    2267
//Total:        182 2117 175.6   2127    2269
//
//Percentage of the requests served within a certain time (ms)
//  50%   2127
//  66%   2135
//  75%   2140
//  80%   2142
//  90%   2148
//  95%   2175
//  98%   2267
//  99%   2268
// 100%   2269 (longest request)

// Unlimited request worker: ab -n 10000 -c 128 "http://localhost:8080/"
//Concurrency Level:      128
//Time taken for tests:   171.476 seconds
//Complete requests:      10000
//Failed requests:        0
//Total transferred:      900000 bytes
//HTML transferred:       110000 bytes
//Requests per second:    58.32 [#/sec] (mean)
//Time per request:       2194.894 [ms] (mean)
//Time per request:       17.148 [ms] (mean, across all concurrent requests)
//Transfer rate:          5.13 [Kbytes/sec] received
//
//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    0   0.4      0       5
//Processing:   600 2183 310.2   2098    4157
//Waiting:      595 2183 310.2   2098    4157
//Total:        601 2183 310.2   2098    4157
//
//Percentage of the requests served within a certain time (ms)
//  50%   2098
//  66%   2123
//  75%   2166
//  80%   2229
//  90%   2450
//  95%   2798
//  98%   3376
//  99%   3784
// 100%   4157 (longest request)
fun main(args: Array<String>) {
//	System.setProperty("reactor.netty.ioWorkerCount", "1");

	val app = SpringApplication(Application::class.java)
	app.run()
}
