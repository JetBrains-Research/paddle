package io.paddle.utils

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json

internal object JSON {
    private val json = Json.Default

    fun <T : Any> string(serializer: SerializationStrategy<T>, value: T): String {
        return json.encodeToString(serializer, value)
    }

    fun <T> parse(serializer: DeserializationStrategy<T>, value: String): T {
        return json.decodeFromString(serializer, value)
    }
}