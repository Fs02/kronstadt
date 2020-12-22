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
	@Query(value = "SELECT SLEEP(0.1) AS result", nativeQuery = true)
	fun sleep(): Int
}

@Controller
class RootController(
		private val sleepRepository: SleepRepository
) {
	//2020-12-21 16:20:05.345  INFO 16892 --- [ctor-http-nio-1] id.asr.kronstadt.e1basic.RootController  : start
	//2020-12-21 16:20:05.415 DEBUG 16892 --- [ctor-http-nio-1] org.hibernate.SQL                        : SELECT SLEEP(0.1) AS result
	//2020-12-21 16:20:05.602  INFO 16892 --- [ctor-http-nio-1] id.asr.kronstadt.e1basic.RootController  : start counting
	//2020-12-21 16:20:05.673 DEBUG 16892 --- [ctor-http-nio-1] org.hibernate.SQL                        : select count(*) as col_0_0_ from sleep sleep0_
	//2020-12-21 16:20:05.676  INFO 16892 --- [ctor-http-nio-1] id.asr.kronstadt.e1basic.RootController  : done
	@GetMapping("/")
	suspend fun get(): ResponseEntity<String> {
//		log.info("start")
		sleepRepository.sleep()

//		log.info("start counting")
		val count = sleepRepository.count()

//		log.info("done")
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

// Single request worker: ab -n 128 -c 128 "http://localhost:8080/"
//Concurrency Level:      128
//Time taken for tests:   22.469 seconds
//Complete requests:      128
//Failed requests:        0
//Total transferred:      11520 bytes
//HTML transferred:       1408 bytes
//Requests per second:    5.70 [#/sec] (mean)
//Time per request:       22469.228 [ms] (mean)
//Time per request:       175.541 [ms] (mean, across all concurrent requests)
//Transfer rate:          0.50 [Kbytes/sec] received
//
//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    3   1.0      3       4
//Processing:   181 10909 6396.0  10880   22026
//Waiting:      181 10908 6396.0  10880   22026
//Total:        185 10911 6395.1  10882   22027
//
//Percentage of the requests served within a certain time (ms)
//  50%  10882
//  66%  14369
//  75%  16506
//  80%  17547
//  90%  19876
//  95%  20954
//  98%  21668
//  99%  21849
// 100%  22027 (longest request)

// Unlimited request worker: ab -n 10000 -c 128 "http://localhost:8080/"
//Concurrency Level:      128
//Time taken for tests:   174.404 seconds
//Complete requests:      10000
//Failed requests:        0
//Total transferred:      900000 bytes
//HTML transferred:       110000 bytes
//Requests per second:    57.34 [#/sec] (mean)
//Time per request:       2232.365 [ms] (mean)
//Time per request:       17.440 [ms] (mean, across all concurrent requests)
//Transfer rate:          5.04 [Kbytes/sec] received
//
//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    0   0.5      0      11
//Processing:   206 2211 243.0   2221    2902
//Waiting:      206 2211 243.0   2221    2902
//Total:        212 2211 242.8   2221    2902
//
//Percentage of the requests served within a certain time (ms)
//  50%   2221
//  66%   2290
//  75%   2356
//  80%   2388
//  90%   2467
//  95%   2575
//  98%   2663
//  99%   2706
// 100%   2902 (longest request)
fun main(args: Array<String>) {
//	System.setProperty("reactor.netty.ioWorkerCount", "1");

	val app = SpringApplication(Application::class.java)
	app.run()
}
