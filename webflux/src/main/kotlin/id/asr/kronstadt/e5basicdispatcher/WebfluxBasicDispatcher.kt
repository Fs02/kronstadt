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
    // 2020-12-21 16:02:29.138  INFO 13850 --- [ctor-http-nio-2] i.a.k.e5basicdispatcher.RootController   : start
    // 2020-12-21 16:02:29.149  INFO 13850 --- [atcher-worker-2] i.a.k.e5basicdispatcher.RootController   : sleep
    // 2020-12-21 16:02:29.250 DEBUG 13850 --- [atcher-worker-2] org.hibernate.SQL                        : SELECT SLEEP(0.1) AS result
    // 2020-12-21 16:02:29.458  INFO 13850 --- [atcher-worker-2] i.a.k.e5basicdispatcher.RootController   : start counting
    // 2020-12-21 16:02:29.539 DEBUG 13850 --- [atcher-worker-2] org.hibernate.SQL                        : select count(*) as col_0_0_ from sleep sleep0_
    // 2020-12-21 16:02:29.541  INFO 13850 --- [atcher-worker-2] i.a.k.e5basicdispatcher.RootController   : done
    @GetMapping("/")
    suspend fun get(): ResponseEntity<String> {
        log.info("start")
        return withContext(Dispatchers.Default) {
            db {
                log.info("sleep")
                sleepRepository.sleep()
            }

            val count = db {
                log.info("start counting")
                sleepRepository.count()
            }

            log.info("done")
            ResponseEntity.ok("count is $count\n")
        }
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
//Connect:        0    5   3.0      5      11
//Processing:   242 2047 163.9   2070    2128
//Waiting:      231 2047 164.8   2070    2128
//Total:        242 2053 163.9   2076    2130
//
//Percentage of the requests served within a certain time (ms)
//  50%   2076
//  66%   2083
//  75%   2091
//  80%   2092
//  90%   2095
//  95%   2096
//  98%   2115
//  99%   2129
// 100%   2130 (longest request)
fun main(args: Array<String>) {
//    System.setProperty("reactor.netty.ioWorkerCount", "1");

    val app = SpringApplication(Application::class.java)
    app.run()
}
