package id.asr.kronstadt.til.timeout

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.util.logging.Logger
import kotlin.system.measureTimeMillis

/**
 * Next: [TimeoutWhenSleepInDifferentThread]
 */
object TimeoutWhenSleeping

fun main(args: Array<String>) {
    runBlocking {
        val time = measureTimeMillis {
            withTimeout(100) {
                Thread.sleep(500)
            }
        }

        println("[${Thread.currentThread().name}] Time: ${time}ms")
    }
}
