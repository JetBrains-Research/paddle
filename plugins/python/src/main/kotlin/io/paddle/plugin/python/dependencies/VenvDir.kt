package io.paddle.plugin.python.dependencies

import io.paddle.plugin.python.dependencies.packages.CachedPyPackage.Companion.PYPACKAGE_CACHE_FILENAME
import io.paddle.plugin.python.dependencies.packages.IResolvedPyPackage
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.extensions.globalInterpreter
import io.paddle.plugin.python.utils.*
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import kotlinx.serialization.decodeFromString
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
            val pythonDir = libDir.listFiles()?.find { it.name.matches(RegexCache.PYTHON_DIR_NAME_REGEX) }
                ?: throw Task.ActException("Incorrect venv structure")
            return pythonDir.resolve("site-packages")
        }

    val pycache: File
        get() = sitePackages.resolve("__pycache__")

    val pyPackageFiles: List<File>
        get() = sitePackages.walkTopDown().filter { it.name == PYPACKAGE_CACHE_FILENAME }.toList()

    val pyPackages: List<PyPackage>
        get() = pyPackageFiles.map { jsonParser.decodeFromString(it.readText()) }

    fun getInterpreterPath(project: PaddleProject): Path {
        return bin.resolve(project.globalInterpreter.resolved.version.executableName).toPath()
    }

    fun hasInstalledPackage(pkg: IResolvedPyPackage): Boolean {
        val infoDir = InstalledPackageInfoDir.findByNameAndVersionOrNull(sitePackages, pkg.name, pkg.version)
        return infoDir?.pkg?.repo == pkg.repo // name and version already matched
    }

    fun findPackageWithNameOrNull(name: PyPackageName): PyPackage? {
        val infoDir = InstalledPackageInfoDir.findByNameOrNull(sitePackages, name)
        return infoDir?.pkg
    }

    fun removePackage(pkg: PyPackage) {
        val affectedDirs = mutableSetOf<File>()
        InstalledPackageInfoDir.findByNameAndVersion(sitePackages, pkg.name, pkg.version).files
            .onEach { if (it.parentFile.isDirectory && it.parentFile != sitePackages && it.parentFile != bin) affectedDirs += it.parentFile }
            .forEach { it.delete() }
        affectedDirs.filter { it.toPath().isEmpty() }.forEach { it.delete() }
    }
}
