package io.paddle.schema.extensions

import io.paddle.project.Project
import io.paddle.utils.resource.ResourceUtils
import java.io.File

abstract class JsonSchemaPart(val destination: String) {
    abstract val content: String
}

class JsonSchemaPartFromString(override val content: String, destination: String) : JsonSchemaPart(destination)

class JsonSchemaPartFromResource(
    resourceName: String,
    destination: String,
    enclosingExtensionObject: Project.Extension<out BaseJsonSchemaExtension>,
    default: String = ""
) : JsonSchemaPart(destination) {
    override val content by ResourceUtils.ResourceContentDelegate(
        "schemas${File.separator}$resourceName",
        enclosingExtensionObject.javaClass.classLoader, default
    )
}
