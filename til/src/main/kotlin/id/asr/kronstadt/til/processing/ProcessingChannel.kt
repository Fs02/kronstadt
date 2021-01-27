package id.asr.kronstadt.til.processing

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Channels are communication primitives that allow us to pass data between different coroutines.
 * One coroutine can send some information to a channel, while the other one can receive this information from it.
 * https://play.kotlinlang.org/hands-on/Introduction%20to%20Coroutines%20and%20Channels/08_Channels
 *
 * Coroutine #1 -> (Channel) -> Coroutine #2
 *
 * Many coroutines can send and receive from the same channel at once:
 *
 * Producer #1 -.             .-> Consumer #1
 * Producer #2 ---> (Channel) --> Consumer #2
 * Producer #3 -'             `-> Consumer #3
 *
 * Different Channel Types:
 * - [Channel.RENDEZVOUS] (Default)
 * - [Channel.BUFFERED]
 * - [Channel.UNLIMITED]
 * - [Channel.CONFLATED]
 *
 * Next: [ProcessingChannelBuffered]
 */
object ProcessingChannel

fun main(args: Array<String>) = runBlocking {
    val channel = Channel<Int>()

    launch {
        for (x in 1..5) {
            println("[${Thread.currentThread().name}] Fetching $x")
            // this will suspend whenever channel is full.
            // backpressure by nature according to: https://stackoverflow.com/a/42126960
            // other method is channel.offer(x) which will return false if channel is full (onBackpressureDrop).
            channel.send(x)
        }
        channel.close()
    }

    // two workers
    repeat(2) {
        launch {
            // will suspend indefinitely until channel is closed.
            for (y in channel) {
                println("[${Thread.currentThread().name}] Started $y")
                delay(1000)
                println("[${Thread.currentThread().name}] Finished $y")
            }
        }
    }
}
