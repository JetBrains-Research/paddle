package io.paddle.plugin.python.extensions

import io.paddle.project.Project
import io.paddle.schema.extensions.BaseJsonSchemaExtension
import io.paddle.schema.extensions.JsonSchemaPart
import io.paddle.utils.ext.Extendable

class JsonSchema(extensions: List<JsonSchemaPart>) : BaseJsonSchemaExtension(extensions) {
    object Extension : Project.Extension<JsonSchema> {
        override val key: Extendable.Key<JsonSchema> = Extendable.Key()

        private fun getJsonSchemaExtensions(): List<JsonSchemaPart> {
            return listOf(
                JsonSchemaPart(
                    "{\"if\":{\"required\":[\"environment\"],\"properties\":{\"environment\":true}},\"then\":{\"properties\":{\"plugins\":{\"properties\":{\"enabled\":{\"contains\":{\"const\":\"python\"}}}}}}}",
                    "/allOf"
                ),
                JsonSchemaPart(
                    "{\"description\":\"Environment that should be used by Paddle for Python build process\",\"properties\":{\"type\":{\"type\":\"string\",\"enum\":[\"virtualenv\",\"global\"]}},\"allOf\":[{\"if\":{\"properties\":{\"type\":{\"const\":\"virtualenv\"}}},\"then\":{\"required\":[\"path\"],\"properties\":{\"path\":{\"description\":\"Path to virtual environment location\",\"type\":\"string\"}}}},{\"if\":{\"properties\":{\"type\":{\"const\":\"global\"}}},\"then\":{}}]}",
                    "/properties/environment"
                )
            )
        }

        override fun create(project: Project): JsonSchema {
            return JsonSchema(getJsonSchemaExtensions())
        }
    }
}
