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
		log.info("start sleeping")
		sleepRepository.sleep()
		log.info("start counting")
		val count =  sleepRepository.count()
		log.info("done")
		return ResponseEntity.ok("count is $count\n")
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
//Connect:        0    4   1.8      4       7
//Processing:   157 10998 6392.4  11133   21801
//Waiting:      157 10998 6392.5  11133   21800
//Total:        164 11003 6390.9  11139   21802
//
//Percentage of the requests served within a certain time (ms)
//  50%  11139
//  66%  14594
//  75%  16680
//  80%  17726
//  90%  19780
//  95%  20757
//  98%  21461
//  99%  21640
// 100%  21802 (longest request)
fun main(args: Array<String>) {
	System.setProperty("server.tomcat.threads.min-spare", "1")
	System.setProperty("server.tomcat.threads.max", "1")

	val app = SpringApplication(Application::class.java)
	app.run()
}
