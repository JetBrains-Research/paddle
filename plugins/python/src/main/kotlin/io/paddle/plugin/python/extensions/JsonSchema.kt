package io.paddle.plugin.python.extensions

import io.paddle.plugin.NamedPluginJsonSchemaExtension
import io.paddle.project.Project
import io.paddle.schema.extensions.AbstractJsonSchemaExtension
import io.paddle.schema.extensions.JsonSchemaPart
import io.paddle.utils.ext.Extendable

class JsonSchema(extensions: List<JsonSchemaPart>) : AbstractJsonSchemaExtension(extensions) {
    object Extension : NamedPluginJsonSchemaExtension<JsonSchema>() {
        override val name: String
            get() = "python"

        override val key: Extendable.Key<JsonSchema> = Extendable.Key()

        override fun getJsonSchemaExtensions(): List<JsonSchemaPart> {
            return super.getJsonSchemaExtensions() + listOf(
                JsonSchemaPart("{\"if\":{\"required\":[\"environment\"],\"properties\":{\"environment\":true}},\"then\":{\"properties\":{\"plugins\":{\"properties\":{\"enabled\":{\"contains\":{\"const\":\"python\"}}}}}}}", "/allOf"),
                JsonSchemaPart("{\"description\":\"Environment that should be used by Paddle for Python build process\",\"properties\":{\"type\":{\"type\":\"string\",\"enum\":[\"virtualenv\",\"global\"]}},\"allOf\":[{\"if\":{\"properties\":{\"type\":{\"const\":\"virtualenv\"}}},\"then\":{\"required\":[\"path\"],\"properties\":{\"path\":{\"description\":\"Path to virtual environment location\",\"type\":\"string\"}}}},{\"if\":{\"properties\":{\"type\":{\"const\":\"global\"}}},\"then\":{}}]}", "/properties/environment")
            )
        }

        override fun create(project: Project): JsonSchema {
            return JsonSchema(getJsonSchemaExtensions())
        }
    }
}
