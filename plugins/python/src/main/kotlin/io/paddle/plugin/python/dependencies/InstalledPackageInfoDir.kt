package io.paddle.plugin.python.dependencies

import io.paddle.plugin.python.dependencies.packages.PyPackageMetadata
import io.paddle.plugin.python.utils.*
import java.io.File
import java.util.function.Predicate

/**
 * A wrapper class for either .dist-info (DIST) and .egg-info (LEGACY) distribution folders.
 */
class InstalledPackageInfoDir(val parentDir: File, val type: Type, val name: PyPackageName, val version: PyPackageVersion) {
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
                ?: error("Neither .dist-info nor .egg-info directory was found in $parentDir for package $name==$version")
        }

        fun extractVersionFromPackageInfoDirname(infoDir: File): PyPackageVersion {
            val prefix = if (infoDir.name.endsWith(".egg-info")) {
                infoDir.name.substringBefore(".egg-info")
            } else {
                infoDir.name.substringBefore(".dist-info")
            }
            return prefix.split("-").getOrNull(1) ?: error("Invalid dirname: ${infoDir.name}")
        }

        private fun findInfoDirWithPredicateOrNull(parentDir: File, predicate: Predicate<File>): File? {
            return parentDir.listFiles()
                ?.filter { predicate.test(it) }
                ?.firstOrNull { it.name.endsWith(".dist-info") || it.name.endsWith(".egg-info") }
        }
    }

    val metadata: PyPackageMetadata by lazy {
        val metadataFilename = if (type == Type.DIST) "METADATA" else "PKG-INFO"
        PyPackageMetadata.parse(parentDir.resolve(metadataFilename))
    }

    val topLevelNames: List<String> by lazy {
        parentDir.resolve("top_level.txt").let {
            if (it.exists()) it.readLines().map { s -> s.trim() } else listOf(name)
        }
    }

    val files: List<File>
        get() = when (type) {
            Type.DIST -> {
                parentDir.resolve("RECORD").readLines()
                    .map { it.split(",")[0] }
                    .map { parentDir.parentFile.resolveRelative(it) }
            }
            Type.LEGACY -> {
                parentDir.resolve("installed-files.txt").readLines()
                    .map { parentDir.resolveRelative(it) }
            }
        }
}
