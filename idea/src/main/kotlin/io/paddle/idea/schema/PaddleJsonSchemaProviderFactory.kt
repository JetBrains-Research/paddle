package io.paddle.idea.schema

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory

class PaddleJsonSchemaProviderFactory : JsonSchemaProviderFactory, DumbAware {
    override fun getProviders(project: Project): List<JsonSchemaFileProvider> {
        return listOf(
            ExtendedJsonSchemaProvider("/schema/paddle-schema.json", "Paddle", setOf("paddle.yaml")),
            ExtendedJsonSchemaProvider("/schema/paddle-auth-schema.json", "Paddle Authentication", setOf("paddle.auth.yaml", "paddle.auth.yaml.example"))
        )
    }
}
