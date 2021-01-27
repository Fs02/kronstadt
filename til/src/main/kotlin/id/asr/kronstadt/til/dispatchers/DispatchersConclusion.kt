package id.asr.kronstadt.til.dispatchers

/**
 * - Dispatchers.Default is not the (real) default.
 * - Coroutines will execute in the parent thread by default.
 * - Coroutine launched inside Dispatchers.Default/IO will be on a different thread than parent.
 * - Dispatchers.Unconfined can be used to confined execution to previous thread.
 *
 * Next: [id.asr.kronstadt.til.timeout.TimeoutTitle]
 */
object DispatchersConclusion
