package io.paddle.plugin.ssh.extensions

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
                    "{\"if\":{\"required\":[\"executor\"],\"properties\":{\"executor\":{\"required\":[\"type\"],\"properties\":{\"type\":{\"const\":\"ssh\"}}}}},\"then\":{\"properties\":{\"plugins\":{\"properties\":{\"enabled\":{\"contains\":{\"const\":\"ssh\"}}}}}}}",
                    "/allOf"
                ),
                JsonSchemaPart("ssh", "/properties/executor/properties/type/enum"),
                JsonSchemaPart(
                    "{\"if\":{\"properties\":{\"type\":{\"const\":\"ssh\"}}},\"then\":{\"required\":[\"user\",\"host\",\"directory\"],\"properties\":{\"user\":{\"description\":\"User to login via ssh\",\"type\":\"string\"},\"host\":{\"description\":\"Host to connect via ssh\",\"type\":\"string\"},\"directory\":{\"description\":\"Absolute path of remote working directory\",\"type\":\"string\"}}}}",
                    "/properties/executor/allOf/type/enum"
                )
            )
        }

        override fun create(project: Project): JsonSchema {
            return JsonSchema(getJsonSchemaExtensions())
        }
    }
}
