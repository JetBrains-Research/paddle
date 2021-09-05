package io.paddle.plugin

import io.paddle.project.Project
import io.paddle.schema.extensions.AbstractJsonSchemaExtension
import io.paddle.schema.extensions.JsonSchemaPart

abstract class NamedPluginJsonSchemaExtension<T : AbstractJsonSchemaExtension> : Project.Extension<T> {
    abstract val name: String

    protected open fun getJsonSchemaExtensions(): List<JsonSchemaPart> = listOf(
        JsonSchemaPart(
            value = name,
            destination = "/properties/plugins/properties/enabled/items/enum"
        )
    )
}
