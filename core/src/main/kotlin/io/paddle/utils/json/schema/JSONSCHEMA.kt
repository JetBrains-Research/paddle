package io.paddle.utils.json.schema

import io.paddle.specification.tree.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.*

internal object JSONSCHEMA {
    private val module = SerializersModule {
        polymorphic(ConfigurationSpecification.SpecTreeNode::class) {
            subclass(CompositeSpecTreeNode::class)
            subclass(ArraySpecTreeNode::class)
            subclass(StringSpecTreeNode::class)
            subclass(IntegerSpecTreeNode::class)
            subclass(BooleanSpecTreeNode::class)
        }
    }

    private val json = Json {
        serializersModule = module
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
    }

    inline fun <reified T : Any> string(value: T): String {
        return json.encodeToString(value)
    }

    inline fun <reified T> parse(value: String): T {
        return json.decodeFromString(value)
    }
}
