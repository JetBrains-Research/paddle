package io.paddle.plugin.python.dependencies.index

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor

private const val THREADS_COUNT = 24
private const val TIMEOUT_MS = 5000L

@ExperimentalSerializationApi
internal val cborParser = Cbor {
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
