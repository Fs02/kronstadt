package id.asr.kronstadt.e5r2dbc

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.getBean
import org.springframework.boot.SpringApplication
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

data class Sleep(
        @Id
        @Column("id")
        var id: Long = 0,
)

@Repository
interface SleepRepository: ReactiveCrudRepository<Sleep, Long> {
    @Query(value = "SELECT SLEEP(2) AS result")
    suspend fun sleep(): Int
}

@EnableR2dbcRepositories
@SpringBootApplication
class Application

// 2782ms
fun main(args: Array<String>) {
    val log = LoggerFactory.getLogger(Application::class.java)
    val app = SpringApplication(Application::class.java)
    app.webApplicationType = WebApplicationType.NONE

    val context = app.run()
    val repo = context.getBean<SleepRepository>()

    val time = measureTimeMillis {
        runBlocking {
            repeat(10) {
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
