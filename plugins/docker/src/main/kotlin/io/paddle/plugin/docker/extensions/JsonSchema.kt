package io.paddle.plugin.docker.extensions

import io.paddle.project.Project
import io.paddle.schema.extensions.*
import io.paddle.utils.ext.Extendable

class JsonSchema(extensions: List<JsonSchemaPart>) : BaseJsonSchemaExtension(extensions) {
    object Extension : Project.Extension<JsonSchema> {
        override val key: Extendable.Key<JsonSchema> = Extendable.Key()

        private fun getJsonSchemaExtensions() = listOf(
            JsonSchemaPartFromResource(
                "docker-constraint.json",
                "/allOf",
                this
            ),
            JsonSchemaPartFromResource(
                "docker-executor.json",
                "/properties/executor/allOf",
                this
            ),
            JsonSchemaPartFromString(
                "docker",
                "/properties/executor/properties/type/enum"
            )
        )

        override fun create(project: Project): JsonSchema {
            return JsonSchema(getJsonSchemaExtensions())
        }
    }
}
