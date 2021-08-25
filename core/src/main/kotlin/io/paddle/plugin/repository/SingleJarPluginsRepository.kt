package io.paddle.plugin.repository

import java.io.File
import java.net.URLClassLoader

class SingleJarPluginsRepository(private val jarFile: File) : AbstractPluginsRepository() {
    override val classLoader: ClassLoader
        get() = URLClassLoader(arrayOf(jarFile.toURI().toURL()), this.javaClass.classLoader)

}
