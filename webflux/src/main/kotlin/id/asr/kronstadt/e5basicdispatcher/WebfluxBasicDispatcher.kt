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
        log.info("start")
        db {
            log.info("sleep")
            sleepRepository.sleep()
        }

        val count = db {
            log.info("start counting")
            sleepRepository.count()
        }

        log.info("done")
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

//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    4   1.7      4       7
//Processing:   189 2017 168.5   2045    2070
//Waiting:      182 2017 169.1   2045    2070
//Total:        189 2021 168.5   2049    2072
//
//Percentage of the requests served within a certain time (ms)
//  50%   2049
//  66%   2061
//  75%   2063
//  80%   2064
//  90%   2065
//  95%   2066
//  98%   2066
//  99%   2066
// 100%   2072 (longest request)
fun main(args: Array<String>) {
    System.setProperty("reactor.netty.ioWorkerCount", "1");

    val app = SpringApplication(Application::class.java)
    app.run()
}
