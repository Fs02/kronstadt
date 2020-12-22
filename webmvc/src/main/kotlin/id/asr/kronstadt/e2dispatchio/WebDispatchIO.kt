package id.asr.kronstadt.e2dispatchio

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
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
    //2020-12-21 17:22:34.014  INFO 19097 --- [nio-8080-exec-1] i.a.k.e2dispatchio.RootController        : start
    //2020-12-21 17:22:34.018  INFO 19097 --- [atcher-worker-1] i.a.k.e2dispatchio.RootController        : start sleeping
    //2020-12-21 17:22:34.092 DEBUG 19097 --- [atcher-worker-1] org.hibernate.SQL                        : SELECT SLEEP(0.1) AS result
    //2020-12-21 17:22:34.281  INFO 19097 --- [atcher-worker-1] i.a.k.e2dispatchio.RootController        : start counting
    //2020-12-21 17:22:34.344 DEBUG 19097 --- [atcher-worker-1] org.hibernate.SQL                        : select count(*) as col_0_0_ from sleep sleep0_
    //2020-12-21 17:22:34.347  INFO 19097 --- [nio-8080-exec-1] i.a.k.e2dispatchio.RootController        : done
    @GetMapping("/")
    fun get(): ResponseEntity<String> = runBlocking {
//        log.info("start")
        db {
//            log.info("start sleeping")
            sleepRepository.sleep()
        }

        val count = db {
//            log.info("start counting")
            sleepRepository.count()
        }

//        log.info("done")
        ResponseEntity.ok("count is $count\n")
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
//Time taken for tests:   21.657 seconds
//Complete requests:      128
//Failed requests:        0
//Total transferred:      18432 bytes
//HTML transferred:       1408 bytes
//Requests per second:    5.91 [#/sec] (mean)
//Time per request:       21656.843 [ms] (mean)
//Time per request:       169.194 [ms] (mean, across all concurrent requests)
//Transfer rate:          0.83 [Kbytes/sec] received
//
//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    3   1.3      3       6
//Processing:   109 10632 6217.2  10818   21231
//Waiting:      108 10632 6217.3  10818   21231
//Total:        114 10636 6216.1  10822   21232
//
//Percentage of the requests served within a certain time (ms)
//  50%  10822
//  66%  14006
//  75%  16107
//  80%  17161
//  90%  19233
//  95%  20190
//  98%  20882
//  99%  21054
// 100%  21232 (longest request)

// Unlimited request worker: ab -n 10000 -c 128 "http://localhost:8080/"
//Concurrency Level:      128
//Time taken for tests:   161.398 seconds
//Complete requests:      10000
//Failed requests:        0
//Total transferred:      1440000 bytes
//HTML transferred:       110000 bytes
//Requests per second:    61.96 [#/sec] (mean)
//Time per request:       2065.894 [ms] (mean)
//Time per request:       16.140 [ms] (mean, across all concurrent requests)
//Transfer rate:          8.71 [Kbytes/sec] received
//
//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    0   0.5      0      11
//Processing:   399 2055 219.4   2033    3795
//Waiting:      394 2055 219.4   2033    3795
//Total:        399 2055 219.4   2034    3797
//
//Percentage of the requests served within a certain time (ms)
//  50%   2034
//  66%   2096
//  75%   2144
//  80%   2176
//  90%   2276
//  95%   2421
//  98%   2605
//  99%   2893
// 100%   3797 (longest request)
fun main(args: Array<String>) {
//    System.setProperty("server.tomcat.threads.min-spare", "1")
//    System.setProperty("server.tomcat.threads.max", "1")

    val app = SpringApplication(Application::class.java)
    app.run()
}
