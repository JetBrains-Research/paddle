package io.paddle.plugin.python.dependencies.index

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.serialization.json.Json

private const val THREADS_COUNT = 24
private const val TIMEOUT_MS = 5000L

internal val jsonParser = Json {
    ignoreUnknownKeys = true
}

internal val httpClient = HttpClient(CIO) {
    engine {
        threadsCount = THREADS_COUNT
        maxConnectionsCount = 1000
        endpoint {
            connectAttempts = 5
            connectTimeout = TIMEOUT_MS
            requestTimeout = TIMEOUT_MS
            socketTimeout = TIMEOUT_MS
        }
    }
}

internal fun Iterable<String>.letters(): Set<Char> = flatMap { it.toSet() }.toSet()

internal fun <T> Iterable<T>.withIndexAt(start: Int) = withIndex().map { IndexedValue(it.index + start, it.value) }

internal fun ByteArray.compare(other: ByteArray) = this.compare(0, size, other)

internal fun ByteArray.compare(start: Int, size: Int, other: ByteArray): Int {
    if (size != other.size) return size - other.size
    //compare elements values
    for (i in 0 until size) {
        val diff = this[i + start] - other[i]

        if (diff != 0) return diff
    }
    return 0
}

private val emptyByteArray = ByteArray(0)
internal fun emptyByteArray() = emptyByteArray
