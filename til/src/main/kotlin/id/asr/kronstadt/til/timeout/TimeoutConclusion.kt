package id.asr.kronstadt.til.timeout

/**
 * Conclusion:
 * - Timeout is basically a cancellation that happens after some period of time.
 * - It won't interrupt any ongoing process, but only prevent the next suspending function to be executed.
 * - Timout 100ms doesn't mean it'll be timout exactly at 100ms, it depends on when coroutine is resumed.
 * - Calling Thread.sleep without any suspension point doesn't cause coroutine to timeout.
 *
 * Next: [id.asr.kronstadt.til.processing.ProcessingTitle]
 */
object TimeoutConclusion
