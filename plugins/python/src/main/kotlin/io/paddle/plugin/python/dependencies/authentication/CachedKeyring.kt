package io.paddle.plugin.python.dependencies.authentication

import com.github.javakeyring.Keyring
import com.github.javakeyring.PasswordAccessException

object CachedKeyring {
    private val keyring = Keyring.create()
    private val cache = HashMap<Pair<String, String>, String>()

    fun getCachedPasswordOrNull(service: String, account: String): String? {
        return cache.getOrElse(service to account) {
            keyring.getPasswordOrNull(service, account)?.also {
                cache[service to account] = it
            }
        }
    }
}

fun Keyring.getPasswordOrNull(service: String, account: String): String? {
    return try {
        getPassword(service, account)
    } catch (e: PasswordAccessException) {
        null
    }
}
