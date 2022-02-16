package io.paddle.plugin.python.dependencies

import io.paddle.plugin.python.dependencies.packages.IResolvedPyPackage
import io.paddle.plugin.python.extensions.interpreter
import io.paddle.plugin.python.utils.RegexCache
import io.paddle.project.Project
import java.io.File
import java.nio.file.Path

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
            val pythonDir = libDir.listFiles()?.find { it.name.matches(RegexCache.PYTHON_DIR_NAME_REGEX) } ?: error("Incorrect venv structure")
            return pythonDir.resolve("site-packages")
        }

    val pycache: File
        get() = sitePackages.resolve("__pycache__")

    fun getInterpreterPath(project: Project): Path {
        return bin.resolve(project.interpreter.resolved.version.executableName).toPath()
    }

    fun hasInstalledPackage(pkg: IResolvedPyPackage): Boolean {
        val infoDir = InstalledPackageInfoDir.findByNameAndVersionOrNull(sitePackages, pkg.name, pkg.version)
        return infoDir?.pkg?.repo == pkg.repo // name and version already matched
    }
}