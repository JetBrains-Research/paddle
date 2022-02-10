package io.paddle.plugin.python.dependencies.index.utils

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.json.Json


internal val jsonParser = Json {
    ignoreUnknownKeys = true
}

class WrappedSerialDescriptor(override val serialName: String, original: SerialDescriptor) : SerialDescriptor by original
