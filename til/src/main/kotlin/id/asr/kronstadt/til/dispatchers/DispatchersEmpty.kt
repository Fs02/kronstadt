package id.asr.kronstadt.til.dispatchers

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

object DispatchersEmpty

fun main(args: Array<String>) {
    runBlocking {
        println("Now I'm running in: ${Thread.currentThread().name}")

        repeat(20) {
            launch {
                println("Running blocking process in: ${Thread.currentThread().name}")
                Thread.sleep(100)
            }
        }
    }
}
