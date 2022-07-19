package io.paddle.idea.schema

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import io.paddle.schema.builder.JsonSchemaBuilder

class ExtendedJsonSchemaProvider(resource: String, presentable: String, filesToApply: Set<String>) :
    EmbeddedJsonSchemaProvider(resource, presentable, filesToApply) {

    override fun getSchemaFile(): VirtualFile? {
        val baseJsonSchema = super.getSchemaFile()
        if (baseJsonSchema != null) {
            val jsonSchemaBuilder = JsonSchemaBuilder(VfsUtil.loadText(baseJsonSchema))
            return LightVirtualFile(name, jsonSchemaBuilder.toString())
        }
        return null
    }
}
