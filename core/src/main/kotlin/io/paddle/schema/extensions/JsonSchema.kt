package io.paddle.schema.extensions

import io.paddle.plugin.standard.extensions.plugins
import io.paddle.project.Project
import io.paddle.utils.ext.Extendable

val Project.jsonSchema: JsonSchema
    get() = extensions.get(JsonSchema.Extension.key)!!

class JsonSchema(val extensions: MutableList<BaseJsonSchemaExtension>) {
    object Extension : Project.Extension<JsonSchema> {
        override val key: Extendable.Key<JsonSchema> = Extendable.Key()

        private fun createSchemaExtensionsBy(names: List<String>): BaseJsonSchemaExtension {
            return BaseJsonSchemaExtension(names.map {
                JsonSchemaPart(
                    value = it,
                    destination = "/properties/plugins/properties/enabled/items/enum"
                )
            })
        }

        override fun create(project: Project): JsonSchema {
            return JsonSchema(mutableListOf(createSchemaExtensionsBy(project.plugins.namesOfAvailable)))
        }
    }
}
