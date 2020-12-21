package id.asr.kronstadt.e1basic

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.stereotype.Repository
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

@Entity
data class Sleep(
		@Id
		@Column(name = "id", nullable = false)
		var id: Long = 0,
)

@Repository
interface SleepRepository: JpaRepository<Sleep, Long> {
	@Query(value = "SELECT SLEEP(2) AS result", nativeQuery = true)
	fun sleep(): Int
}

@EnableJpaRepositories
@SpringBootApplication
class Application

fun main(args: Array<String>) {
	val log = LoggerFactory.getLogger(Application::class.java)
	val app = SpringApplication(Application::class.java)
	app.webApplicationType = WebApplicationType.NONE

	val context = app.run()
	val repo = context.getBean(SleepRepository::class.java)

	val time = measureTimeMillis {
		runBlocking {
			repeat(2) {
				launch {
					log.info("start number: $it")
					val sleep = repo.sleep()
					repo.count()
					log.info("result number: $it is $sleep")
					log.info("done number: $it")
				}
			}
		}
	}

	println("time: ${time}ms")
	exitProcess(0)
}
