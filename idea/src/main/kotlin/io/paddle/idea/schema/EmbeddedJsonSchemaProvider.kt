package io.paddle.idea.schema

import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.*

open class EmbeddedJsonSchemaProvider(private val resource: String, private val presentable: String, private val filesToApply: Set<String>) :
    JsonSchemaFileProvider {
    private val schema = JsonSchemaProviderFactory.getResourceFile(EmbeddedJsonSchemaProvider::class.java, resource)

    override fun isAvailable(file: VirtualFile): Boolean = file.name in filesToApply

    override fun getName(): String = resource

    override fun getSchemaFile(): VirtualFile? = schema

    override fun getSchemaType(): SchemaType = SchemaType.embeddedSchema

    override fun getPresentableName(): String = presentable

    override fun isUserVisible(): Boolean = true

    override fun getRemoteSource(): String? = null
}
