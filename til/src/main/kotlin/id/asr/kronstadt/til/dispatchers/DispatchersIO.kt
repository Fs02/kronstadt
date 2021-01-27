package id.asr.kronstadt.til.dispatchers

import kotlinx.coroutines.*

/**
 * Doc:
 * The CoroutineDispatcher that is designed for offloading blocking IO tasks to a shared pool of threads.
 *
 * Next: [DispatchersUnconfined]
 */
object DispatchersIO

fun main(args: Array<String>) {
    runBlocking(Dispatchers.Default) {
        println("Now I'm running in: ${Thread.currentThread().name}")

        // how this will be executed? with how many threads?
        repeat(20) {
            launch(Dispatchers.IO) {
                println("Running blocking process in: ${Thread.currentThread().name}")
                Thread.sleep(100)
            }
        }
    }
}
