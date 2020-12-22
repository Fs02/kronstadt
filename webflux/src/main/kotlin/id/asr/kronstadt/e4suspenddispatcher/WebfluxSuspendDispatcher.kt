package id.asr.kronstadt.e4suspenddispatcher

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.slf4j.MDC
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
import org.springframework.web.reactive.config.EnableWebFlux
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
    //2020-12-21 16:40:54.306  INFO 18240 --- [atcher-worker-2] i.a.k.e.RootController                   : start sleeping
    //2020-12-21 16:40:54.311  INFO 18240 --- [atcher-worker-2] o.s.s.concurrent.ThreadPoolTaskExecutor  : Initializing ExecutorService 'applicationTaskExecutor'
    //2020-12-21 16:40:54.351 DEBUG 18240 --- [         task-1] org.hibernate.SQL                        : SELECT SLEEP(0.1) AS result
    //2020-12-21 16:40:54.540  INFO 18240 --- [atcher-worker-2] i.a.k.e.RootController                   : start counting
    //2020-12-21 16:40:54.589 DEBUG 18240 --- [         task-2] org.hibernate.SQL                        : select count(*) as col_0_0_ from sleep sleep0_
    //2020-12-21 16:40:54.592  INFO 18240 --- [atcher-worker-2] i.a.k.e.RootController                   : done
    @GetMapping("/")
    suspend fun get(): ResponseEntity<String> {
        return withContext(Dispatchers.Default) {
//            log.info("start")
            sleepRepository.sleep().await()

//            log.info("start counting")
            val count = sleepRepository.count().await()

//            log.info("done")
            ResponseEntity.ok("count is $count\n")
        }
    }

    companion object {
        val log = LoggerFactory.getLogger(RootController::class.java)
    }
}

@EnableAsync
@EnableWebFlux
@EnableJpaRepositories
@SpringBootApplication
class Application

// Single request worker: ab -n 128 -c 128 "http://localhost:8080/"
//Concurrency Level:      128
//Time taken for tests:   3.244 seconds
//Complete requests:      128
//Failed requests:        0
//Total transferred:      11520 bytes
//HTML transferred:       1408 bytes
//Requests per second:    39.46 [#/sec] (mean)
//Time per request:       3244.033 [ms] (mean)
//Time per request:       25.344 [ms] (mean, across all concurrent requests)
//Transfer rate:          3.47 [Kbytes/sec] received
//
//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    3   1.2      3       5
//Processing:   494 2665 202.8   2699    2749
//Waiting:      488 2665 203.2   2698    2749
//Total:        494 2668 202.8   2702    2750
//
//Percentage of the requests served within a certain time (ms)
//  50%   2702
//  66%   2729
//  75%   2737
//  80%   2743
//  90%   2747
//  95%   2748
//  98%   2749
//  99%   2750
// 100%   2750 (longest request)

// Unlimited request worker: ab -n 10000 -c 128 "http://localhost:8080/"
//Concurrency Level:      128
//Time taken for tests:   206.874 seconds
//Complete requests:      10000
//Failed requests:        0
//Total transferred:      900000 bytes
//HTML transferred:       110000 bytes
//Requests per second:    48.34 [#/sec] (mean)
//Time per request:       2647.985 [ms] (mean)
//Time per request:       20.687 [ms] (mean, across all concurrent requests)
//Transfer rate:          4.25 [Kbytes/sec] received
//
//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    0   0.4      0       5
//Processing:   432 2637 288.7   2587    5055
//Waiting:      427 2637 288.7   2587    5055
//Total:        433 2637 288.7   2587    5057
//
//Percentage of the requests served within a certain time (ms)
//  50%   2587
//  66%   2617
//  75%   2641
//  80%   2683
//  90%   2941
//  95%   3273
//  98%   3509
//  99%   3784
// 100%   5057 (longest request)
fun main(args: Array<String>) {
//    System.setProperty("reactor.netty.ioWorkerCount", "1");

    val app = SpringApplication(Application::class.java)
    app.run()
}
