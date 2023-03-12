package io.paddle.plugin.python.utils

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.Json


internal val jsonParser = Json {
    ignoreUnknownKeys = true
}

@ExperimentalSerializationApi
class WrappedSerialDescriptor(override val serialName: String, original: SerialDescriptor) :
    SerialDescriptor by original
