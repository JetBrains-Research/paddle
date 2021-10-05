package io.paddle.plugin.python.extensions

import io.paddle.project.Project
import io.paddle.schema.extensions.*
import io.paddle.utils.ext.Extendable

class JsonSchema(extensions: List<JsonSchemaPart>) : BaseJsonSchemaExtension(extensions) {
    object Extension : Project.Extension<JsonSchema> {
        override val key: Extendable.Key<JsonSchema> = Extendable.Key()

        private fun getJsonSchemaExtensions() = listOf(
            JsonSchemaPartFromResource(
                "environment-constraint.json",
                "/allOf",
                this
            ),
            JsonSchemaPartFromResource(
                "environment.json",
                "/properties/environment",
                this
            ),
            JsonSchemaPartFromResource(
                "requirements-constraint.json",
                "/allOf",
                this
            ),
            JsonSchemaPartFromResource(
                "requirements.json",
                "/properties/requirements", this
            )
        )

        override fun create(project: Project): JsonSchema {
            return JsonSchema(getJsonSchemaExtensions())
        }
    }
}
