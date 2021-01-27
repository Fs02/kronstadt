package id.asr.kronstadt.til.basic

/**
 * What is coroutine?
 * - ... light-weight threads (kotlin).
 * - ... a function that can suspend execution to be resumed later (c++20).
 * - ... a function that has the ability to pause execution and return control (Unity C#).
 * - ... coroutine suspension point is explicit using yield function (and suspend modifier in kotlin case).
 *
 * vs goroutine?
 * - goroutine is not a normal (if not just similar to) coroutine.
 * - goroutine have scheduler.
 * - goroutine implicitly surrender control and doesn't have explicit control like yield or suspend.
 *
 * vs fiber?
 * - coroutine doesn't have a concept called scheduler.
 * - fiber is user-space thread that managed by app-level scheduler.
 * - C++ boost.fiber is actually implemented on top of boost.coroutine
 * http://www.open-std.org/jtc1/sc22/wg21/docs/papers/2014/n4024.pdf
 *
 * Next: [BasicAsyncIsCallback]
 */
object BasicTitle
