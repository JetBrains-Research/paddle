package io.paddle.plugin.python.dependencies

import io.paddle.plugin.python.dependencies.packages.CachedPyPackage.Companion.PYPACKAGE_CACHE_FILENAME
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.dependencies.packages.PyPackageMetadata
import io.paddle.plugin.python.utils.*
import kotlinx.serialization.decodeFromString
import java.io.File
import java.io.FileOutputStream
import java.util.function.Predicate

/**
 * A wrapper class for either .dist-info (DIST) and .egg-info (LEGACY) distribution folders.
 */
class InstalledPackageInfoDir(val dir: File, val type: Type, val name: PyPackageName, val version: PyPackageVersion) {
    companion object {
        enum class Type { DIST, LEGACY }

        fun findByNameAndVersionOrNull(parentDir: File, name: PyPackageName, version: PyPackageVersion): InstalledPackageInfoDir? {
            val infoDir = findInfoDirWithPredicateOrNull(parentDir) {
                it.isDirectory &&
                    (it.name.startsWith("$name-$version") ||
                        it.name.startsWith("${name.normalize()}-$version") ||
                        it.name.startsWith("${name.denormalize()}-$version"))
            } ?: return null
            val type = if (infoDir.name.endsWith(".dist-info")) Type.DIST else Type.LEGACY
            return InstalledPackageInfoDir(infoDir, type, name, version)
        }

        fun findByNameAndVersion(parentDir: File, name: PyPackageName, version: PyPackageVersion): InstalledPackageInfoDir {
            return findByNameAndVersionOrNull(parentDir, name, version)
                ?: error("Neither .dist-info nor .egg-info directory was found in $parentDir for package $name==$version.")
        }

        fun findIfSingle(parentDir: File): InstalledPackageInfoDir {
            val infoDir = findInfoDirWithPredicateOrNull(parentDir) { true }
                ?: error("Neither .dist-info nor .egg-info directory was found in $parentDir for package.")
            return InstalledPackageInfoDir(
                infoDir,
                type = if (infoDir.name.endsWith(".dist-info")) Type.DIST else Type.LEGACY,
                name = infoDir.nameWithoutExtension.substringBeforeLast('-'),
                version = infoDir.nameWithoutExtension.substringAfterLast('-')
            )
        }

        private fun findInfoDirWithPredicateOrNull(parentDir: File, predicate: Predicate<File>): File? {
            return parentDir.listFiles()
                ?.filter { predicate.test(it) }
                ?.firstOrNull { it.name.endsWith(".dist-info") || it.name.endsWith(".egg-info") }
        }
    }

    val metadata: PyPackageMetadata by lazy {
        val metadataFilename = if (type == Type.DIST) "METADATA" else "PKG-INFO"
        PyPackageMetadata.parse(dir.resolve(metadataFilename))
    }

    val topLevelNames: List<String> by lazy {
        dir.resolve("top_level.txt").let {
            if (it.exists()) it.readLines().map { s -> s.trim() } else listOf(name)
        }
    }

    val files: List<File>
        get() = when (type) {
            Type.DIST -> {
                dir.resolve("RECORD").readLines()
                    .map { it.split(",")[0] }
                    .map { dir.parentFile.resolveRelative(it) }
            }
            Type.LEGACY -> {
                dir.resolve("installed-files.txt").readLines()
                    .map { dir.resolveRelative(it) }
            }
        }

    fun addFile(name: String, content: String) {
        val targetFile = dir.resolve(name)
        targetFile.writeText(content)
        when (type) {
            Type.DIST -> {
                FileOutputStream(dir.resolve("RECORD"), true).bufferedWriter().use {
                    it.write(dir.name + File.separatorChar + name)
                }
            }
            Type.LEGACY -> {
                FileOutputStream(dir.resolve("installed-files.txt"), true).bufferedWriter().use {
                    it.write(name)
                }
            }
        }
    }

    val pkg: PyPackage?
        get() {
            val file = dir.resolve(PYPACKAGE_CACHE_FILENAME)
            return if (file.exists()) jsonParser.decodeFromString(file.readText()) else null
        }
}
