package io.paddle.plugin.python.utils

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.utils.hash.StringHashable


typealias PyUrl = String
typealias PyPackagesRepositoryUrl = PyUrl
typealias PyPackageUrl = PyUrl

private const val THREADS_COUNT = 24
private const val TIMEOUT_MS = 20000L

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

class CachedHttpClient private constructor(val client: HttpClient, val credentials: PyPackageRepository.Credentials) {
    companion object {
        @Volatile
        private var instance: CachedHttpClient? = null

        fun getInstance(credentials: PyPackageRepository.Credentials): HttpClient =
            if (credentials == PyPackageRepository.Credentials.EMPTY)
                httpClient
            else
                instance?.client ?: synchronized(this) {
                    instance?.client ?: authenticateClient(credentials).also { instance = it }.client
                }

        private fun authenticateClient(credentials: PyPackageRepository.Credentials): CachedHttpClient {
            return CachedHttpClient(
                httpClient.config {
                    install(Auth) {
                        basic {
                            credentials {
                                BasicAuthCredentials(username = credentials.login, password = credentials.password)
                            }
                        }
                    }
                },
                credentials
            )
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

fun PyUrl.trimmedEquals(other: PyUrl): Boolean {
    return this.trimEnd('/') == other.trimEnd('/')
}

fun PyPackagesRepositoryUrl.getDefaultName(): String {
    val urlSimple = this.getSimple()
    return urlSimple.split("/").takeLast(2).getOrNull(0)
        ?: StringHashable(urlSimple).hash()
}

fun PyPackagesRepositoryUrl.getSimple(): String {
    return if (!this.trim('/').endsWith("simple")) this.join("simple") else this
}

fun PyPackagesRepositoryUrl.removeSimple(): String {
    return this.removeSuffix("/").removeSuffix("/simple")
}

fun PyPackagesRepositoryUrl.getHost(): String {
    return if (contains("@")) {
        substringAfter("@").substringBefore("/")
    } else {
        substringAfter("://").substringBefore("/")
    }
}

fun PyUrl.getSecure(): String {
    val (protocol, uriWithToken) = split("://")
    val uri = uriWithToken.substringAfter("@")
    return "$protocol://$uri"
}


