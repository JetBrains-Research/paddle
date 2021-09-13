package io.paddle.plugin.python.dependencies

import io.paddle.plugin.python.extensions.Requirements
import java.io.File

/**
 * A decorator class that contains utilities for accessing python's venv subdirectories.
 *
 * https://docs.python.org/3/library/venv.html#creating-virtual-environments
 */
class VenvDir(private val directory: File) : File(directory.path) {
    val bin: File
        get() = directory.resolve("bin")

    val sitePackages: File
        get() {
            val libDir = directory.resolve("lib")
            val pythonDir = libDir.listFiles()?.find { it.name.matches(Regex("python\\d.\\d")) } ?: error("Incorrect venv structure")
            return pythonDir.resolve("site-packages")
        }

    fun hasInstalledPackage(dependency: Requirements.Descriptor): Boolean {
        return this.sitePackages.listFiles()
            ?.any { it.isDirectory && it.name == dependency.distInfoDirName }
            ?: false
    }
}
