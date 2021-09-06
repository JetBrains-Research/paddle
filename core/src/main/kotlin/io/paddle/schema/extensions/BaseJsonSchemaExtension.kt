package io.paddle.schema.extensions

import io.paddle.schema.JsonSchemaBuilder

data class JsonSchemaPart(val value: String, val destination: String)

open class BaseJsonSchemaExtension(private val extensions: List<JsonSchemaPart>) {
    open fun applyTo(schemaBuilder: JsonSchemaBuilder) {
        extensions.forEach { schemaBuilder.append(it) }
    }
}
