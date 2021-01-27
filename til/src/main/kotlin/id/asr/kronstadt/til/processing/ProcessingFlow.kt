package id.asr.kronstadt.til.processing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.runBlocking

/**
 * Flow in a way is very similar to flux:
 * - it's can handle stream of values
 * - it's cold
 * - it's best use case is to write complex flow of processing.
 *
 * but doesn't have any ads regarding back pressure.
 *
 * vs channel: emit is suspended until value is collected,
 *             while in channel it only suspended until value is received.
 *
 * Next: [ProcessingFlowOut]
 */
object ProcessingFlow

fun main(args: Array<String>) = runBlocking {
    val task = flow {
        for (x in 1..5) {
            emit(x)
            println("[${Thread.currentThread().name}] Submitted $x")
        }
    }

    task
            .buffer(2)
            .flowOn(Dispatchers.Default)
            .collect {
                println("[${Thread.currentThread().name}] Started $it")
                delay(1000)
                println("[${Thread.currentThread().name}] Finished $it")
            }
}
