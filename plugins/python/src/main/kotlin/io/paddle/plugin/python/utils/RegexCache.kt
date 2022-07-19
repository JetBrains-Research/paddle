package io.paddle.plugin.python.utils

internal object RegexCache {
    val UPPER_CASE_SPLIT_REGEX = Regex("(?=\\p{Upper})")
    val PYTHON_DIR_NAME_REGEX = Regex("python\\d.\\d") // FIXME
    val PYTHON_VERSION_REGEX = Regex("[23](.[0-9]+)*")
    val PYTHON_EXECUTABLE_REGEX = Regex("python([23](.[0-9]+)*)?")
}
