package id.asr.kronstadt.til.basic

import kotlinx.coroutines.future.await
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking

/**
 * In theory, it's possible to make kotlin coroutine to work with any asynchronous interface (because of callback).
 *
 * Next: [BasicConclusion]
 */
object BasicSuspendingInterop

fun main(args: Array<String>) = runBlocking {
    var resF = addFuture(4, 1).await()
    resF = subFuture(resF, 2).await()
    resF = mulFuture(resF, 3).await()
    println("[${Thread.currentThread().name}] Future: $resF")

    // works for mono as well
    var resM = addMono(4, 1).awaitSingle()
    resM = subMono(resM, 2).awaitSingle()
    resM = mulMono(resM, 3).awaitSingle()
    println("[${Thread.currentThread().name}] Mono: $resM")

    // we can write a code using coroutine that returns mono as well.
    val otherM = mono {
        var res = add(4, 1)
        res = sub(res, 2)
        mul(res, 3)
    }
    otherM.subscribe {
        println("[${Thread.currentThread().name}] Wrapped Mono: $it")
    }

    Unit
}
