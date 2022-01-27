package io.paddle.plugin.python.utils

internal object RegexCache {
    val UPPER_CASE_SPLIT_REGEX by lazy { Regex("(?=\\p{Upper})") }
    val PYTHON_DIR_NAME_REGEX by lazy { Regex("python\\d.\\d") }
    val PYTHON_VERSION_REGEX by lazy { Regex("[123](.[0-9]+)*") }
}
