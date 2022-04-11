package io.paddle.plugin.repositories

import java.io.File
import java.net.URLClassLoader

class JarPluginsRepository(val name: String, private val jarFile: File) : AbstractJVMBasedPluginsRepository() {
    override val classLoader: ClassLoader
        get() = URLClassLoader(arrayOf(jarFile.toURI().toURL()), this.javaClass.classLoader)
}
