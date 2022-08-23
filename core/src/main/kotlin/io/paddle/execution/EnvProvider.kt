package io.paddle.execution

fun interface EnvProvider {
    fun get(key: String): String?
}
