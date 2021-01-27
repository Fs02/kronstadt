package id.asr.kronstadt.til.processing

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Can we do fan-out using flow?
 *
 * We can, but there's no way to limit the number of worker coroutine (as far as I know).
 * but we can still limit the number of thread.
 *
 * Next: [ProcessingConclusion]
 */
object ProcessingFlowOut

@FlowPreview
fun main(args: Array<String>) = runBlocking {
    val task = flow {
        for (x in 1..5) {
            emit(x)
            println("[${Thread.currentThread().name}] Submitted $x")
        }
    }

    task
            .buffer(1)
            .flatMapMerge(2) {
                flow {
                    println("[${Thread.currentThread().name}] Started $it")
                    delay(1000)
                    println("[${Thread.currentThread().name}] Finished $it")
                    emit(Unit)
                }
            }
            .collect()
}
