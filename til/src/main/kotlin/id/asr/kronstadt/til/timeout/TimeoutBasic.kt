package id.asr.kronstadt.til.timeout

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.system.measureTimeMillis

/**
 * Next: [TimeoutWhenThereIsLongDelay]
 */
object TimeoutBasic

fun main(args: Array<String>) {
    runBlocking {
        val time = measureTimeMillis {
            withTimeout(100) {
                repeat(10) {
                    delay(10)
                    println("[${Thread.currentThread().name}] ${it+1}")
                }
            }
        }

        println("[${Thread.currentThread().name}] Time: ${time}ms")
    }
}
