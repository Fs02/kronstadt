package id.asr.kronstadt.til.processing

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Producer can send values to buffered channel until the capacity is full.
 * When capacity is full, producer will be suspended until the space becomes available again.
 *
 * Producer #1 -> ([ ][ ][+]) -> Consumer #1
 *
 * Buffered channel is useful when our worker is slow and
 * we don't want the worker to wait for another task when it finished previous task.
 * with buffered channel, we can deposit some task that the worker can consume.
 *
 * Next: [ProcessingChannelUnlimited]
 */
object ProcessingChannelBuffered

fun main(args: Array<String>) = runBlocking {
    val channel = Channel<Int>(2)

    launch {
        for (x in 1..5) {
            println("[${Thread.currentThread().name}] Fetching batch $x")
            // contains more than 1 element.
            (x*10..x*10+1).forEach { channel.send(it) }
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
