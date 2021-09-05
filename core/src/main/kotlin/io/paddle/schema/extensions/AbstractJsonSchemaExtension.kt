package io.paddle.schema.extensions

import io.paddle.schema.JsonSchemaBuilder

data class JsonSchemaPart(val value: String, val destination: String)

abstract class AbstractJsonSchemaExtension(private val extensions: List<JsonSchemaPart>) {
    fun applyTo(schemaBuilder: JsonSchemaBuilder) {
        extensions.forEach { schemaBuilder.append(it) }
    }
}
