package id.asr.kronstadt.til.basic

import kotlinx.coroutines.*

/**
 * Coroutine provides a convention for managing async execution.
 *
 * Using coroutine scope, we can define an asynchronous execution that only exists within specific scope.
 * By convention, they all wait for all the coroutines inside their block to complete before completing themselves,
 * thus enforcing the structured concurrency
 */
object BasicStructuredConcurrency

fun main(args: Array<String>) = runBlocking {
    process()
}

suspend fun process() {
    coroutineScope {
        val deferredResult = async {
            delay(500)
            println("async")
            1
        }
        launch {
            delay(1000)
            println("launch")
        }
        launch {
            delay(300)
            println("other launch")
        }
        println("result ${deferredResult.await()}")
    }

    println("finished")
}
