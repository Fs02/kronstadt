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
        log.info("start sleeping")
        sleepRepository.sleep().await()
        log.info("start counting")
        val count =  sleepRepository.count().await()
        log.info("done")
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

//Connection Times (ms)
//              min  mean[+/-sd] median   max
//Connect:        0    4   1.6      4       6
//Processing:   176 10731 6328.6  10833   21589
//Waiting:      170 10730 6328.7  10833   21589
//Total:        176 10735 6327.2  10837   21590
//
//Percentage of the requests served within a certain time (ms)
//  50%  10837
//  66%  14251
//  75%  16267
//  80%  17277
//  90%  19541
//  95%  20589
//  98%  21235
//  99%  21412
// 100%  21590 (longest request)
fun main(args: Array<String>) {
    System.setProperty("server.tomcat.threads.min-spare", "1")
    System.setProperty("server.tomcat.threads.max", "1")

    val app = SpringApplication(Application::class.java)
    app.run()
}
