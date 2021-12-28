package io.paddle.plugin.python.utils

import io.ktor.client.*
import io.ktor.client.engine.cio.*


typealias PyUrl = String
typealias PyPackagesRepositoryUrl = PyUrl
typealias PyPackageUrl = PyUrl

private const val THREADS_COUNT = 24
private const val TIMEOUT_MS = 5000L

internal val httpClient = HttpClient(CIO) {
    followRedirects = true
    expectSuccess = false
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

fun PyUrl.join(urlPart: String): String {
    return "${this.trimEnd('/')}/${urlPart.trimEnd('/')}/"
}

fun PyUrl.join(vararg urlParts: String): String {
    var result = this
    for (urlPart in urlParts) {
        result = result.join(urlPart)
    }
    return result
}


