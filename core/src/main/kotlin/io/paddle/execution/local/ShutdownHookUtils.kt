package io.paddle.execution.local

internal object ShutdownHookUtils {
    fun addShutDownHookSilently(hook: Thread) {
        try {
            Runtime.getRuntime().addShutdownHook(hook)
        } catch (ignore: IllegalStateException) {
        }
    }

    fun removeShutdownHookSilently(hook: Thread?) {
        try {
            Runtime.getRuntime().removeShutdownHook(hook)
        } catch (ignore: IllegalStateException) {
        }
    }
}