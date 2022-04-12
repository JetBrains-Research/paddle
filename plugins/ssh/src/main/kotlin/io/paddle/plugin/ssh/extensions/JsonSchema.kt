package io.paddle.plugin.ssh.extensions

import io.paddle.project.PaddleProject
import io.paddle.schema.extensions.*
import io.paddle.utils.ext.Extendable

class JsonSchema(extensions: List<JsonSchemaPart>) : BaseJsonSchemaExtension(extensions) {
    object Extension : PaddleProject.Extension<JsonSchema> {
        override val key: Extendable.Key<JsonSchema> = Extendable.Key()

        private fun getExtensions() = listOf(
            JsonSchemaPartFromString(
                "ssh",
                "/properties/executor/properties/type/enum"
            ),
            JsonSchemaPartFromResource(
                "ssh-constraint.json",
                "/allOf",
                this
            ),
            JsonSchemaPartFromResource(
                "ssh-executor.json",
                "/properties/executor/allOf",
                this
            )
        )

        override fun create(project: PaddleProject): JsonSchema {
            return JsonSchema(getExtensions())
        }
    }
}
