package io.paddle.schema.extensions

import io.paddle.schema.builder.JsonSchemaBuilder

open class BaseJsonSchemaExtension(private val jsonSchemaParts: List<JsonSchemaPart>) {
    open fun applyTo(schemaBuilder: JsonSchemaBuilder) {
        jsonSchemaParts.forEach { schemaBuilder.append(it) }
    }
}
