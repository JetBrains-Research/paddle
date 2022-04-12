package io.paddle.schema.extensions

import io.paddle.project.PaddleProject
import io.paddle.utils.jar.JarUtils
import java.io.File

abstract class JsonSchemaPart(val destination: String) {
    abstract val content: String
}

class JsonSchemaPartFromString(override val content: String, destination: String) : JsonSchemaPart(destination)

class JsonSchemaPartFromResource(
    resourceName: String,
    destination: String,
    enclosingExtensionObject: PaddleProject.Extension<out BaseJsonSchemaExtension>,
    default: String = ""
) : JsonSchemaPart(destination) {
    override val content by JarUtils.ResourceContentDelegate(
        "schemas${File.separator}$resourceName",
        enclosingExtensionObject.javaClass.classLoader, default
    )
}
