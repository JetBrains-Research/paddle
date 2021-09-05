package io.paddle.schema.extensions

import io.paddle.project.Project
import io.paddle.utils.ext.Extendable

val Project.jsonSchema: JsonSchema
    get() = extensions.get(JsonSchema.Extension.key)!!

class JsonSchema(val extensions: MutableList<AbstractJsonSchemaExtension>) {
    object Extension : Project.Extension<JsonSchema> {
        override val key: Extendable.Key<JsonSchema> = Extendable.Key()

        override fun create(project: Project) = JsonSchema(mutableListOf())
    }
}
