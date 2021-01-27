package id.asr.kronstadt.til.dispatchers

import kotlinx.coroutines.*

/**
 * Doc:
 * The default CoroutineDispatcher that is used by all standard builders like launch, async, etc
 * if neither a dispatcher nor any other ContinuationInterceptor is specified in their context.
 *
 * Next: [DispatchersIO]
 */
object DispatchersDefault

fun main(args: Array<String>) {
    runBlocking {
        println("By default I'm running in: ${Thread.currentThread().name}")

        withContext(Dispatchers.Default) {
            println("Now I'm running in: ${Thread.currentThread().name}")

            // how this will be executed? with how many threads?
            repeat(20) {
                launch {
                    println("Running blocking process in: ${Thread.currentThread().name}")
                    Thread.sleep(100)
                }
            }
        }
    }
}
