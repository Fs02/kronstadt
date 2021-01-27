package id.asr.kronstadt.til.timeout

import kotlinx.coroutines.future.await
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.util.concurrent.CompletableFuture
import kotlin.system.measureTimeMillis

/**
 * Next: [TimeoutConclusion]
 */
object TimeoutWhenSleepInDifferentThread

fun main(args: Array<String>) {
    runBlocking {
        val time = measureTimeMillis {
            withTimeout(100) {
                CompletableFuture.runAsync {
                    println("[${Thread.currentThread().name}] Start")
                    Thread.sleep(500)
                    println("[${Thread.currentThread().name}] Finish")
                }.await()
            }
        }

        println("[${Thread.currentThread().name}] Time: ${time}ms")
    }
}




