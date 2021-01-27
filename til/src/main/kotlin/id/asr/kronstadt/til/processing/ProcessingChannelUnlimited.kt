package id.asr.kronstadt.til.processing

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * With unlimited mode, we can flood our channel even if the worker can't keep up.
 * It'll grow indefinitely until OOM.
 *
 * Producer #1 -> ([+][+]...[+]]) -> Consumer #1
 *
 * Next: [ProcessingChannelConflated]
 */
object ProcessingChannelUnlimited

fun main(args: Array<String>) = runBlocking {
    val channel = Channel<Int>(Channel.UNLIMITED)

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
