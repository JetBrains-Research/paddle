package io.paddle.plugin.python.dependencies

import kotlinx.coroutines.*
import java.net.URL

fun String.isValidUrl(): Boolean = try {
    URL(this).toURI()
    true
} catch (e: Exception) {
    false
}

suspend fun <A> Iterable<A>.parallelForEach(action: suspend (A) -> Unit): Unit = coroutineScope {
    map { launch { action(it) } }.joinAll()
}
