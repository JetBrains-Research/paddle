package io.paddle.plugin.python.utils

import kotlinx.serialization.KSerializer
import java.io.File

class Cache<K, V>(
    private val storage: File,
    private val serializer: KSerializer<Map<K, V>>,
) {
    private var cache: Map<K, V>
        get() {
            storage.parentFile.mkdirs()
            return storage.takeIf { it.exists() }
                ?.let { jsonParser.decodeFromString(serializer, it.readText()) }
                ?: emptyMap()
        }
        set(value) {
            storage.parentFile.mkdirs()
            storage.writeText(jsonParser.encodeToString(serializer, value))
        }

    fun getFromCache(input: K) = cache[input]

    @Synchronized
    fun updateCache(input: K, output: V) {
        cache = cache.toMutableMap().also { it[input] = output }
    }
}

fun <K, V, T> cached(storage: File, serializer: KSerializer<Map<K, V>>, use: Cache<K, V>.() -> T): T {
    val cache = Cache(storage, serializer)
    return cache.use()
}
