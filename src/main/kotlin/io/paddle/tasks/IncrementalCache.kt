package io.paddle.tasks

import io.paddle.utils.Hashable
import io.paddle.utils.JSON
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import java.io.File

object IncrementalCache {
    private val storage = File(".paddle/cache.json")

    private var cache: Map<String, Cache>
        get() {
            storage.parentFile.mkdirs()
            return storage.takeIf { it.exists() }
                ?.let { JSON.parse(MapSerializer(String.serializer(), Cache.serializer()), it.readText()) }
                ?: emptyMap()
        }
        set(value) {
            storage.parentFile.mkdirs()
            storage.writeText(JSON.string(MapSerializer(String.serializer(), Cache.serializer()), value))
        }

    @Serializable
    private data class Cache(val input: String, val output: String)

    fun isUpToDate(id: String, input: Hashable, output: Hashable): Boolean {
        val current = cache[id] ?: return false
        return current.input == input.hash() && current.output == output.hash()
    }

    @Synchronized
    fun update(id: String, input: Hashable, output: Hashable) {
        cache = cache.toMutableMap().also {
            it[id] = Cache(input.hash(), output.hash())
        }
    }
}