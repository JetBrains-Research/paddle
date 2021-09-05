package io.paddle.plugin.ssh.extensions

import io.paddle.plugin.NamedPluginJsonSchemaExtension
import io.paddle.project.Project
import io.paddle.schema.extensions.AbstractJsonSchemaExtension
import io.paddle.schema.extensions.JsonSchemaPart
import io.paddle.utils.ext.Extendable

class JsonSchema(extensions: List<JsonSchemaPart>) : AbstractJsonSchemaExtension(extensions) {
    object Extension : NamedPluginJsonSchemaExtension<JsonSchema>() {
        override val name: String
            get() = "ssh"

        override val key: Extendable.Key<JsonSchema> = Extendable.Key()

        override fun getJsonSchemaExtensions(): List<JsonSchemaPart> {
            return super.getJsonSchemaExtensions() + listOf(
                JsonSchemaPart("{\"if\":{\"required\":[\"executor\"],\"properties\":{\"executor\":{\"required\":[\"type\"],\"properties\":{\"type\":{\"const\":\"ssh\"}}}}},\"then\":{\"properties\":{\"plugins\":{\"properties\":{\"enabled\":{\"contains\":{\"const\":\"ssh\"}}}}}}}", "/allOf"
                ),
                JsonSchemaPart("ssh", "/properties/executor/properties/type/enum"),
                JsonSchemaPart("{\"if\":{\"properties\":{\"type\":{\"const\":\"ssh\"}}},\"then\":{\"required\":[\"user\",\"host\",\"directory\"],\"properties\":{\"user\":{\"description\":\"User to login via ssh\",\"type\":\"string\"},\"host\":{\"description\":\"Host to connect via ssh\",\"type\":\"string\"},\"directory\":{\"description\":\"Absolute path of remote working directory\",\"type\":\"string\"}}}}", "/properties/executor/allOf/type/enum")
            )
        }

        override fun create(project: Project): JsonSchema {
            return JsonSchema(getJsonSchemaExtensions())
        }
    }
}
