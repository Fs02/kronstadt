package id.asr.kronstadt.til.basic

import kotlinx.coroutines.runBlocking

/**
 * Suspend under the hood also just a callback.
 * It'll transform every suspending function to have `Continuation<T>` arguments on compile time.
 * Example:
 *  add(a: Int, b: Int): Int
 *
 * Become:
 *  add(a: Int, b: Int, Continuation<T>): Int
 *
 * And because of that, the end result is conceptually similar to reactive, where each processes is divided to small functions.
 * but in a more traditional looking code.
 *
 * Next: [BasicSuspendingInterop]
 */
object BasicSuspending

suspend fun add(a: Int, b: Int): Int {
    println("[${Thread.currentThread().name}] $a + $b = ${a+b}")
    return a+b
}

suspend fun sub(a: Int, b: Int): Int {
    println("[${Thread.currentThread().name}] $a - $b = ${a-b}")
    return a-b
}

suspend fun mul(a: Int, b: Int): Int {
    println("[${Thread.currentThread().name}] $a * $b = ${a*b}")
    return a*b
}

fun main(args: Array<String>) = runBlocking {
    var res = add(4, 1)
    res = sub(res, 2)
    res = mul(res, 3)
    println("[${Thread.currentThread().name}] result: $res")
}
