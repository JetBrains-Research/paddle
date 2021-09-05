package io.paddle.utils.jar

import java.io.File
import kotlin.io.path.outputStream

object JarUtils {
    fun getResourceFileBy(classLoader: ClassLoader, resourcePath: String): File? {
        val tempFile = kotlin.io.path.createTempFile()
        return classLoader.getResourceAsStream(resourcePath)
            ?.let {
                it.copyTo(tempFile.outputStream())
                tempFile.toFile()
            }
            ?.also { it.deleteOnExit() }
    }
}
