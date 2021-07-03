package io.paddle.utils

fun String.splitAndTrim(vararg delimiters: String): List<String> {
    return this.split(*delimiters).map { it.trim() }.filter { it.isNotBlank() }
}
