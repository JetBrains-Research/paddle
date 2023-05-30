package io.paddle.utils.config

object PaddleAppRuntime {
    val isTests: Boolean by lazy {
        for (element in Thread.currentThread().stackTrace) {
            if (element.className.startsWith("org.junit.")) {
                return@lazy true
            }
        }
        return@lazy false
    }
}
