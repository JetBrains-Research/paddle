package io.paddle.plugin.python.dependencies

import io.paddle.plugin.python.extensions.Requirements
import io.paddle.plugin.python.utils.PyPackageVersion
import io.paddle.plugin.python.utils.resolveRelative
import java.io.File
import java.util.function.Predicate

/**
 * A wrapper class for either .dist-info (DIST) and .egg-info (LEGACY) distribution folders.
 */
class InstalledPackageInfo(val parentDir: File, val type: Type, val descriptor: Requirements.Descriptor) {
    companion object {
        enum class Type { DIST, LEGACY }

        fun findByDescriptorOrNull(parentDir: File, descriptor: Requirements.Descriptor): InstalledPackageInfo? {
            val infoDir = findInfoDirWithPredicateOrNull(parentDir) { it.isDirectory && it.name.startsWith(descriptor.infoDirPrefix) } ?: return null
            val type = if (infoDir.name.endsWith(".dist-info")) Type.DIST else Type.LEGACY
            return InstalledPackageInfo(infoDir, type, descriptor)
        }

        fun findByDescriptor(parentDir: File, descriptor: Requirements.Descriptor): InstalledPackageInfo {
            return findByDescriptorOrNull(parentDir, descriptor)
                ?: error("Neither .dist-info nor .egg-info directory was found in $parentDir for package $descriptor")
        }

        fun findByPackageNameOrNull(parentDir: File, pkgName: String): InstalledPackageInfo? {
            val infoDir = findInfoDirWithPredicateOrNull(parentDir) { it.isDirectory && it.name.startsWith("$pkgName-") } ?: return null
            val type = if (infoDir.name.endsWith(".dist-info")) Type.DIST else Type.LEGACY
            val pkgVersion = extractVersionFromPackageInfoDirname(infoDir)
            return InstalledPackageInfo(infoDir, type, Requirements.Descriptor(pkgName, pkgVersion))
        }

        fun findByPackageName(parentDir: File, pkgName: String): InstalledPackageInfo {
            return findByPackageNameOrNull(parentDir, pkgName)
                ?: error("Neither .dist-info nor .egg-info directory was found in $parentDir for package $pkgName")
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

    val pkgVersion: PyPackageVersion = extractVersionFromPackageInfoDirname(parentDir)

    val metadata: PackageMetadata by lazy {
        val metadataFilename = if (type == Type.DIST) "METADATA" else "PKG-INFO"
        PackageMetadata.parse(parentDir.resolve(metadataFilename))
    }

    val topLevelNames: List<String> by lazy {
        parentDir.resolve("top_level.txt").let {
            if (it.exists()) it.readLines().map { s -> s.trim() } else listOf(descriptor.name)
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
