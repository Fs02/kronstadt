package id.asr.kronstadt.e5basicdispatcher

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

suspend fun <T> db(
        block: suspend CoroutineScope.() -> T
) = withContext(Dispatchers.IO, block)

@Controller
class RootController(
        private val sleepRepository: SleepRepository
) {
    //2020-12-21 17:27:42.611  INFO 19375 --- [ctor-http-nio-1] i.a.k.e5basicdispatcher.RootController   : start
    //2020-12-21 17:27:42.617  INFO 19375 --- [atcher-worker-2] i.a.k.e5basicdispatcher.RootController   : sleep
    //2020-12-21 17:27:42.711 DEBUG 19375 --- [atcher-worker-2] org.hibernate.SQL                        : SELECT SLEEP(0.1) AS result
    //2020-12-21 17:27:42.904  INFO 19375 --- [atcher-worker-2] i.a.k.e5basicdispatcher.RootController   : start counting
    //2020-12-21 17:27:42.955 DEBUG 19375 --- [atcher-worker-2] org.hibernate.SQL                        : select count(*) as col_0_0_ from sleep sleep0_
    //2020-12-21 17:27:42.958  INFO 19375 --- [atcher-worker-2] i.a.k.e5basicdispatcher.RootController   : done
    @GetMapping("/")
    suspend fun get(): ResponseEntity<String> {
//        log.info("start")
        db {
//            log.info("sleep")
            sleepRepository.sleep()
        }

        val count = db {
//            log.info("start counting")
            sleepRepository.count()
        }

//        log.info("done")
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
//Time taken for tests:   2.730 seconds
//Complete requests:      128
//Failed requests:        0
//Total transferred:      11520 bytes
//HTML transferred:       1408 bytes
//Requests per second:    46.89 [#/sec] (mean)
//Time per request:       2729.827 [ms] (mean)
//Time per request:       21.327 [ms] (mean, across all concurrent requests)
//Transfer rate:          4.12 [Kbytes/sec] received
//
//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    3   1.2      3       5
//Processing:   515 2095 142.3   2111    2214
//Waiting:      510 2095 142.7   2111    2214
//Total:        515 2098 142.4   2114    2215
//
//Percentage of the requests served within a certain time (ms)
//  50%   2114
//  66%   2119
//  75%   2121
//  80%   2123
//  90%   2125
//  95%   2126
//  98%   2127
//  99%   2174
// 100%   2215 (longest request)

// Unlimited request worker: ab -n 10000 -c 128 "http://localhost:8080/"
//Concurrency Level:      128
//Time taken for tests:   165.789 seconds
//Complete requests:      10000
//Failed requests:        0
//Total transferred:      900000 bytes
//HTML transferred:       110000 bytes
//Requests per second:    60.32 [#/sec] (mean)
//Time per request:       2122.104 [ms] (mean)
//Time per request:       16.579 [ms] (mean, across all concurrent requests)
//Transfer rate:          5.30 [Kbytes/sec] received
//
//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    0   0.5      0      15
//Processing:   493 2111 225.2   2066    3979
//Waiting:      487 2111 225.2   2066    3979
//Total:        494 2111 225.2   2067    3980
//
//Percentage of the requests served within a certain time (ms)
//  50%   2067
//  66%   2114
//  75%   2166
//  80%   2205
//  90%   2374
//  95%   2540
//  98%   2727
//  99%   2914
// 100%   3980 (longest request)
fun main(args: Array<String>) {
//    System.setProperty("reactor.netty.ioWorkerCount", "1");

    val app = SpringApplication(Application::class.java)
    app.run()
}
