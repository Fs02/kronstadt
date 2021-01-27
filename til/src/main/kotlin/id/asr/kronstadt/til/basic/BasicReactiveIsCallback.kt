package id.asr.kronstadt.til.basic

import reactor.core.publisher.Mono

/**
 * Asynchronous code is usually implemented as callback, and this also applies to reactor.
 *
 * Reactive have more benefit that it can handle big traffic efficiently.
 * How?
 * Dividing execution to multiple small operators (ex: flatMap) helps utilizing the resource better and allow for concurrent execution.
 *
 * Next: [BasicSuspending]
 */
object BasicReactiveIsCallback

fun addMono(a: Int, b: Int): Mono<Int> = Mono.defer {
    println("[${Thread.currentThread().name}] $a + $b = ${a+b}")
    Mono.just(a+b)
}

fun subMono(a: Int, b: Int): Mono<Int> = Mono.defer {
    println("[${Thread.currentThread().name}] $a - $b = ${a-b}")
    Mono.just(a-b)
}

fun mulMono(a: Int, b: Int): Mono<Int> = Mono.defer {
    println("[${Thread.currentThread().name}] $a * $b = ${a*b}")
    Mono.just(a*b)
}

fun main(args: Array<String>) {
    // thanks to flat map, it's better
    addMono(4, 1)
        .flatMap {
            subMono(it, 2)
        }
        .flatMap {
            mulMono(it, 3)
        }
        .subscribe {
            println("[${Thread.currentThread().name}] result: $it")
        }
}
