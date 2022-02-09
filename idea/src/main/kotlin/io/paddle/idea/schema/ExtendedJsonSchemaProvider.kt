package io.paddle.idea.schema

import com.intellij.testFramework.LightVirtualFile
import io.paddle.specification.JsonSchemaSpecification
import io.paddle.idea.utils.PaddleProject

class ExtendedJsonSchemaProvider(resource: String, presentable: String, filesToApply: Set<String>) :
    EmbeddedJsonSchemaProvider(resource, presentable, filesToApply) {

    override fun getSchemaFile(): LightVirtualFile? {
        val baseSchema = super.getSchemaFile()
        if (baseSchema != null) {
            val configSpec = PaddleProject.currentProject?.configSpec
            return if (configSpec is JsonSchemaSpecification) LightVirtualFile(name, configSpec.build()) else null
        }
        return baseSchema
    }
}
