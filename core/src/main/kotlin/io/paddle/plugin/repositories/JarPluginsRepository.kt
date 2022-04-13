package io.paddle.plugin.repositories

import io.paddle.plugin.LocalPluginsRepoName
import java.io.File
import java.net.URLClassLoader


class JarPluginsRepository(val name: LocalPluginsRepoName, private val jarFile: File) : AbstractJVMBasedPluginsRepository() {
    override val classLoader: ClassLoader
        get() = URLClassLoader(arrayOf(jarFile.toURI().toURL()), this.javaClass.classLoader)
}
