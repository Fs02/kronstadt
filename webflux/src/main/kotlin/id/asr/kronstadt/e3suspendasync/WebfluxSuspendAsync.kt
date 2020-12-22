package id.asr.kronstadt.e3suspendasync

import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactive.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.slf4j.MDCContext
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
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.reactive.config.EnableWebFlux
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
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
//
//@Component
//class ContextFilter: WebFilter {
//    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
//        return mono(MDCContext()) {
//            MDC.put("starting-thread", Thread.currentThread().name)
//            chain.filter(exchange).awaitSingleOrNull()
//        }
//    }
//}

@Controller
class RootController(
        private val sleepRepository: SleepRepository
) {
    //2020-12-21 16:25:36.979  INFO 17396 --- [atcher-worker-1] i.a.k.e3suspendasync.RootController      : source: DefaultDispatcher-worker-1 start sleeping
    //2020-12-21 16:25:36.980 DEBUG 17396 --- [         task-3] org.hibernate.SQL                        : SELECT SLEEP(0.1) AS result
    //2020-12-21 16:25:37.155  INFO 17396 --- [         task-3] i.a.k.e3suspendasync.RootController      : source: null start counting
    //2020-12-21 16:25:37.157 DEBUG 17396 --- [         task-4] org.hibernate.SQL                        : select count(*) as col_0_0_ from sleep sleep0_
    //2020-12-21 16:25:37.159  INFO 17396 --- [         task-4] i.a.k.e3suspendasync.RootController      : source: null done
    @GetMapping("/")
    suspend fun get(): ResponseEntity<String> {
//        log.info("start")
        sleepRepository.sleep().await()

//        log.info("start counting")
        val count = sleepRepository.count().await()

//        log.info("done")
        return ResponseEntity.ok("count is $count\n")
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
//Time taken for tests:   3.224 seconds
//Complete requests:      128
//Failed requests:        0
//Total transferred:      11520 bytes
//HTML transferred:       1408 bytes
//Requests per second:    39.70 [#/sec] (mean)
//Time per request:       3223.826 [ms] (mean)
//Time per request:       25.186 [ms] (mean, across all concurrent requests)
//Transfer rate:          3.49 [Kbytes/sec] received
//
//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    6   2.1      6       9
//Processing:   470 2647 207.6   2683    2752
//Waiting:      462 2647 208.3   2683    2751
//Total:        471 2653 207.5   2689    2753
//
//Percentage of the requests served within a certain time (ms)
//  50%   2689
//  66%   2707
//  75%   2716
//  80%   2719
//  90%   2727
//  95%   2730
//  98%   2742
//  99%   2743
// 100%   2753 (longest request)

// Unlimited request worker: ab -n 10000 -c 128 "http://localhost:8080/"
//Concurrency Level:      128
//Time taken for tests:   210.569 seconds
//Complete requests:      10000
//Failed requests:        0
//Total transferred:      900000 bytes
//HTML transferred:       110000 bytes
//Requests per second:    47.49 [#/sec] (mean)
//Time per request:       2695.290 [ms] (mean)
//Time per request:       21.057 [ms] (mean, across all concurrent requests)
//Transfer rate:          4.17 [Kbytes/sec] received
//
//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    0   0.5      0       6
//Processing:   375 2683 305.3   2646    5334
//Waiting:      370 2683 305.3   2646    5334
//Total:        376 2683 305.3   2646    5335
//
//Percentage of the requests served within a certain time (ms)
//  50%   2646
//  66%   2697
//  75%   2731
//  80%   2766
//  90%   2941
//  95%   3198
//  98%   3465
//  99%   3809
// 100%   5335 (longest request)
fun main(args: Array<String>) {
//    System.setProperty("reactor.netty.ioWorkerCount", "1");

    val app = SpringApplication(Application::class.java)
    app.run()
}
