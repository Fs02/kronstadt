package id.asr.kronstadt.til.timeout

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.util.logging.Logger
import kotlin.system.measureTimeMillis

/**
 * Next: [TimeoutWhenSleeping]
 */
object TimeoutWhenThereIsLongDelay

fun main(args: Array<String>) {
    runBlocking {
        val time = measureTimeMillis {
            withTimeout(100) {
                delay(500)
            }
        }

        println("[${Thread.currentThread().name}] Time: ${time}ms")
    }
}
