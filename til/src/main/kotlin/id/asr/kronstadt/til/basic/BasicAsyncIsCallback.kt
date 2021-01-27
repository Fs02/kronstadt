package id.asr.kronstadt.til.basic

import java.util.concurrent.CompletableFuture

/**
 * Asynchronous code is usually implemented as callback.
 *
 * Next: [BasicReactiveIsCallback]
 */
object BasicAsyncIsCallback

fun addFuture(a: Int, b: Int): CompletableFuture<Int> = CompletableFuture.supplyAsync {
    println("[${Thread.currentThread().name}] $a + $b = ${a+b}")
    a+b
}

fun subFuture(a: Int, b: Int): CompletableFuture<Int> = CompletableFuture.supplyAsync {
    println("[${Thread.currentThread().name}] $a - $b = ${a-b}")
    a-b
}

fun mulFuture(a: Int, b: Int): CompletableFuture<Int> = CompletableFuture.supplyAsync {
    println("[${Thread.currentThread().name}] $a * $b = ${a*b}")
    a*b
}

fun main(args: Array<String>) {
    val job = addFuture(4, 1).thenApply {
        subFuture(it, 2).thenApply {
            mulFuture(it, 3).thenApply {
                println("[${Thread.currentThread().name}] result: $it")
            }
        }
    }.join()
}
