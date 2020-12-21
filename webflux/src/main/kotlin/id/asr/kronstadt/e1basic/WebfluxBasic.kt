package id.asr.kronstadt.e1basic

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

@Controller
class RootController(
		private val sleepRepository: SleepRepository
) {
	//2020-12-21 16:20:05.345  INFO 16892 --- [ctor-http-nio-1] id.asr.kronstadt.e1basic.RootController  : start
	//2020-12-21 16:20:05.415 DEBUG 16892 --- [ctor-http-nio-1] org.hibernate.SQL                        : SELECT SLEEP(0.1) AS result
	//2020-12-21 16:20:05.602  INFO 16892 --- [ctor-http-nio-1] id.asr.kronstadt.e1basic.RootController  : start counting
	//2020-12-21 16:20:05.673 DEBUG 16892 --- [ctor-http-nio-1] org.hibernate.SQL                        : select count(*) as col_0_0_ from sleep sleep0_
	//2020-12-21 16:20:05.676  INFO 16892 --- [ctor-http-nio-1] id.asr.kronstadt.e1basic.RootController  : done
	@GetMapping("/")
	suspend fun get(): ResponseEntity<String> {
		log.info("start")
		sleepRepository.sleep()

		log.info("start counting")
		val count = sleepRepository.count()

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
//Connect:        0    3   1.3      3       5
//Processing:   180 10832 6343.6  10957   21759
//Waiting:      180 10832 6343.6  10957   21759
//Total:        185 10835 6342.3  10961   21760
//
//Percentage of the requests served within a certain time (ms)
//  50%  10961
//  66%  14275
//  75%  16363
//  80%  17425
//  90%  19661
//  95%  20691
//  98%  21401
//  99%  21580
// 100%  21760 (longest request)
fun main(args: Array<String>) {
	System.setProperty("reactor.netty.ioWorkerCount", "1");

	val app = SpringApplication(Application::class.java)
	app.run()
}
