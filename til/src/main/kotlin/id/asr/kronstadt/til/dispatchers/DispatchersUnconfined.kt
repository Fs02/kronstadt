package id.asr.kronstadt.til.dispatchers

import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import java.util.concurrent.CompletableFuture

/**
 * A coroutine dispatcher that is not confined to any specific thread.
 * It executes the initial continuation of a coroutine in the current call-frame
 * and lets the coroutine resume in whatever thread that is used by the corresponding suspending function,
 * without mandating any specific threading policy.
 * Nested coroutines launched in this dispatcher form an event-loop to avoid stack overflows.
 *
 * Next: [DispatchersConclusion]
 */
object DispatchersUnconfined

fun main(args: Array<String>) {
    runBlocking(Dispatchers.Unconfined) {
        println("1. Now I'm running in: ${Thread.currentThread().name}")

        CompletableFuture.runAsync {
            println("2. Future is in: ${Thread.currentThread().name}")

            launch {
                println("3. Launch in future is in: ${Thread.currentThread().name}")
                delay(10)
                println("4. Finish Launch in future is in: ${Thread.currentThread().name}")
            }
        }.await()

        println("5. Now I'm running in: ${Thread.currentThread().name}")
    }
}
