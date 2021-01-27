package id.asr.kronstadt.til.processing

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Conflated channel will drop previously sent element when it's full.
 *
 * Next: [ProcessingFlow]
 */
object ProcessingChannelConflated

fun main(args: Array<String>) = runBlocking {
    val channel = Channel<Int>(Channel.CONFLATED)

    launch {
        for (x in 1..5) {
            println("[${Thread.currentThread().name}] Fetching $x")
            channel.send(x)
        }
        channel.close()
    }

    // two workers
    repeat(2) {
        launch {
            for (y in channel) {
                println("[${Thread.currentThread().name}] Processing $y")
                delay(1000)
            }
        }
    }
}
