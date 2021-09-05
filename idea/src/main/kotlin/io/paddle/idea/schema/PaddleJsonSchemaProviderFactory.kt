package io.paddle.idea.schema

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider

class PaddleJsonSchemaProviderFactory : JsonSchemaProviderFactory, DumbAware {
    override fun getProviders(project: Project): List<JsonSchemaFileProvider> {
        return listOf(
            ExtendedJsonSchemaProvider("/schema/paddle-schema.json", "Paddle", setOf("paddle.yaml"))
        )
    }
}
