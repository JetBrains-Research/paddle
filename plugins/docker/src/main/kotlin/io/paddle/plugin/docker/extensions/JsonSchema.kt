package io.paddle.plugin.docker.extensions

import io.paddle.plugin.NamedPluginJsonSchemaExtension
import io.paddle.project.Project
import io.paddle.schema.extensions.AbstractJsonSchemaExtension
import io.paddle.schema.extensions.JsonSchemaPart
import io.paddle.utils.ext.Extendable

class JsonSchema(extensions: List<JsonSchemaPart>) : AbstractJsonSchemaExtension(extensions) {
    object Extension : NamedPluginJsonSchemaExtension<JsonSchema>() {
        override val name: String
            get() = "docker"

        override val key: Extendable.Key<JsonSchema> = Extendable.Key()

        override fun getJsonSchemaExtensions(): List<JsonSchemaPart> {
            return super.getJsonSchemaExtensions() + listOf(
                JsonSchemaPart("{\"if\":{\"required\":[\"executor\"],\"properties\":{\"executor\":{\"required\":[\"type\"],\"properties\":{\"type\":{\"const\":\"docker\"}}}}},\"then\":{\"properties\":{\"plugins\":{\"properties\":{\"enabled\":{\"contains\":{\"const\":\"docker\"}}}}}}}", "/allOf"),
                JsonSchemaPart("docker", "/properties/executor/properties/type/enum"),
                JsonSchemaPart("{\"if\":{\"properties\":{\"type\":{\"const\":\"docker\"}}},\"then\":{\"required\":[\"image\"],\"properties\":{\"image\":{\"description\":\"Image to be used for build\",\"type\":\"string\"}}}}", "/properties/executor/allOf")
            )
        }

        override fun create(project: Project): JsonSchema {
            return JsonSchema(getJsonSchemaExtensions())
        }
    }
}
