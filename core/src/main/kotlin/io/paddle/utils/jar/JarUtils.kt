package io.paddle.utils.jar

import io.paddle.schema.extensions.JsonSchemaPartFromResource
import java.io.File
import java.net.URL
import kotlin.io.path.outputStream
import kotlin.reflect.KProperty

object JarUtils {
    class ResourceContentDelegate(private val resourcePath: String, private val classLoader: ClassLoader, private val default: String) {
        operator fun getValue(thisRef: JsonSchemaPartFromResource, property: KProperty<*>): String {
            return getResourceFileBy(classLoader, resourcePath)?.readText() ?: default
        }
    }

    private fun createFileFor(resource: URL): File {
        val tempFile = kotlin.io.path.createTempFile()
        resource.openStream().copyTo(tempFile.outputStream())
        return tempFile.toFile().also { it.deleteOnExit() }
    }

    fun getResourceFileBy(classLoader: ClassLoader, resourcePath: String): File? {
        return classLoader.getResource(resourcePath)?.let { createFileFor(it) }
    }

    fun getResourcesFilesBy(classLoader: ClassLoader, resourcePath: String): List<File?> {
        return classLoader.getResources(resourcePath).toList().map { createFileFor(it) }
    }
}
