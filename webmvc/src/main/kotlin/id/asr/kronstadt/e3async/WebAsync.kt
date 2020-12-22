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
    @Query(value = "SELECT SLEEP(0.1) AS result", nativeQuery = true)
    fun sleep(): CompletableFuture<Int>

    fun count(): CompletableFuture<Long>
}

@Controller
class RootController(
        private val sleepRepository: SleepRepository
) {
    //2020-12-21 16:12:55.120  INFO 16197 --- [nio-8080-exec-1] id.asr.kronstadt.e3async.RootController  : start sleeping
    //2020-12-21 16:12:55.160 DEBUG 16197 --- [         task-1] org.hibernate.SQL                        : SELECT SLEEP(0.1) AS result
    //2020-12-21 16:12:55.350  INFO 16197 --- [nio-8080-exec-1] id.asr.kronstadt.e3async.RootController  : start counting
    //2020-12-21 16:12:55.424 DEBUG 16197 --- [         task-2] org.hibernate.SQL                        : select count(*) as col_0_0_ from sleep sleep0_
    //2020-12-21 16:12:55.434  INFO 16197 --- [nio-8080-exec-1] id.asr.kronstadt.e3async.RootController  : done
    @GetMapping("/")
    fun get(): ResponseEntity<String> = runBlocking {
//        log.info("start sleeping")
        sleepRepository.sleep().await()
//        log.info("start counting")
        val count =  sleepRepository.count().await()
//        log.info("done")
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

// Single request worker: ab -n 128 -c 128 "http://localhost:8080/"
//Concurrency Level:      128
//Time taken for tests:   22.094 seconds
//Complete requests:      128
//Failed requests:        0
//Total transferred:      18432 bytes
//HTML transferred:       1408 bytes
//Requests per second:    5.79 [#/sec] (mean)
//Time per request:       22093.748 [ms] (mean)
//Time per request:       172.607 [ms] (mean, across all concurrent requests)
//Transfer rate:          0.81 [Kbytes/sec] received
//
//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    3   1.3      3       6
//Processing:   178 10820 6390.4  10843   21687
//Waiting:      178 10820 6390.5  10843   21686
//Total:        183 10824 6389.3  10846   21688
//
//Percentage of the requests served within a certain time (ms)
//  50%  10846
//  66%  14378
//  75%  16504
//  80%  17565
//  90%  19682
//  95%  20698
//  98%  21325
//  99%  21504
// 100%  21688 (longest request)

// Unlimited request worker: ab -n 10000 -c 128 "http://localhost:8080/"
//Concurrency Level:      128
//Time taken for tests:   206.669 seconds
//Complete requests:      10000
//Failed requests:        0
//Total transferred:      1440000 bytes
//HTML transferred:       110000 bytes
//Requests per second:    48.39 [#/sec] (mean)
//Time per request:       2645.362 [ms] (mean)
//Time per request:       20.667 [ms] (mean, across all concurrent requests)
//Transfer rate:          6.80 [Kbytes/sec] received
//
//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    0   0.4      0       5
//Processing:   353 2636 368.4   2550    5052
//Waiting:      347 2636 368.4   2550    5052
//Total:        353 2636 368.4   2550    5052
//
//Percentage of the requests served within a certain time (ms)
//  50%   2550
//  66%   2587
//  75%   2603
//  80%   2615
//  90%   2878
//  95%   3283
//  98%   4133
//  99%   4412
// 100%   5052 (longest request)
fun main(args: Array<String>) {
//    System.setProperty("server.tomcat.threads.min-spare", "1")
//    System.setProperty("server.tomcat.threads.max", "1")

    val app = SpringApplication(Application::class.java)
    app.run()
}
