package id.asr.kronstadt.e1basic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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
	//2020-12-21 16:08:14.805  INFO 14503 --- [nio-8080-exec-1] id.asr.kronstadt.e1basic.RootController  : start sleeping
	//2020-12-21 16:08:14.860 DEBUG 14503 --- [nio-8080-exec-1] org.hibernate.SQL                        : SELECT SLEEP(0.1) AS result
	//2020-12-21 16:08:14.985  INFO 14503 --- [nio-8080-exec-1] id.asr.kronstadt.e1basic.RootController  : start counting
	//2020-12-21 16:08:15.030 DEBUG 14503 --- [nio-8080-exec-1] org.hibernate.SQL                        : select count(*) as col_0_0_ from sleep sleep0_
	//2020-12-21 16:08:15.035  INFO 14503 --- [nio-8080-exec-1] id.asr.kronstadt.e1basic.RootController  : done
	@GetMapping("/")
	fun get(): ResponseEntity<String> {
//		log.info("start sleeping")
		sleepRepository.sleep()
//		log.info("start counting")
		val count =  sleepRepository.count()
//		log.info("done")
		return ResponseEntity.ok("count is $count\n")
	}

	companion object {
		val log = LoggerFactory.getLogger(RootController::class.java)
	}
}

@EnableJpaRepositories
@SpringBootApplication
class Application

// Single request worker: ab -n 128 -c 128 "http://localhost:8080/"
//Concurrency Level:      128
//Time taken for tests:   22.569 seconds
//Complete requests:      128
//Failed requests:        0
//Total transferred:      18432 bytes
//HTML transferred:       1408 bytes
//Requests per second:    5.67 [#/sec] (mean)
//Time per request:       22569.169 [ms] (mean)
//Time per request:       176.322 [ms] (mean, across all concurrent requests)
//Transfer rate:          0.80 [Kbytes/sec] received
//
//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    3   1.3      3       6
//Processing:   181 11116 6500.7  11261   22164
//Waiting:      180 11115 6500.8  11261   22163
//Total:        186 11119 6499.6  11264   22165
//
//Percentage of the requests served within a certain time (ms)
//  50%  11264
//  66%  14808
//  75%  16881
//  80%  17926
//  90%  20028
//  95%  21100
//  98%  21810
//  99%  21988
// 100%  22165 (longest request)

// Unlimited request worker: ab -n 10000 -c 128 "http://localhost:8080/"
//Concurrency Level:      128
//Time taken for tests:   174.719 seconds
//Complete requests:      10000
//Failed requests:        0
//Total transferred:      1440000 bytes
//HTML transferred:       110000 bytes
//Requests per second:    57.23 [#/sec] (mean)
//Time per request:       2236.401 [ms] (mean)
//Time per request:       17.472 [ms] (mean, across all concurrent requests)
//Transfer rate:          8.05 [Kbytes/sec] received
//
//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    0   0.5      0      29
//Processing:   175 2216 179.6   2246    4349
//Waiting:      174 2216 179.7   2246    4349
//Total:        175 2216 179.4   2247    4349
//
//Percentage of the requests served within a certain time (ms)
//  50%   2247
//  66%   2253
//  75%   2271
//  80%   2273
//  90%   2291
//  95%   2300
//  98%   2314
//  99%   2322
// 100%   4349 (longest request)
fun main(args: Array<String>) {
//	System.setProperty("server.tomcat.threads.min-spare", "1")
//	System.setProperty("server.tomcat.threads.max", "1")

	val app = SpringApplication(Application::class.java)
	app.run()
}
