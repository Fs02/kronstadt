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
        log.info("start")
        db {
            log.info("start sleeping")
            sleepRepository.sleep()
        }

        val count = db {
            log.info("start counting")
            sleepRepository.count()
        }

        log.info("done")
        ResponseEntity.ok("count is $count\n")
    }

    companion object {
        val log = LoggerFactory.getLogger(RootController::class.java)
    }
}

@EnableJpaRepositories
@SpringBootApplication
class Application

//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    4   1.7      4       7
//Processing:   182 10768 6367.0  10832   21679
//Waiting:      175 10768 6367.2  10832   21679
//Total:        182 10772 6365.5  10836   21680
//
//Percentage of the requests served within a certain time (ms)
//  50%  10836
//  66%  14300
//  75%  16362
//  80%  17399
//  90%  19617
//  95%  20649
//  98%  21331
//  99%  21503
// 100%  21680 (longest request)
fun main(args: Array<String>) {
    System.setProperty("server.tomcat.threads.min-spare", "1")
    System.setProperty("server.tomcat.threads.max", "1")

    val app = SpringApplication(Application::class.java)
    app.run()
}
