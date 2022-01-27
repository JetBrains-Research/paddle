package io.paddle.plugin.python.dependencies

import io.paddle.plugin.python.dependencies.index.PyPackagesRepository
import io.paddle.plugin.python.extensions.Requirements
import io.paddle.plugin.python.utils.PyPackageName
import io.paddle.plugin.python.utils.PyPackageVersion
import java.io.File
import java.nio.file.Path

/**
 * Cached version of package, stored in global cache folder.
 *
 * Common structure:
 *  - BIN/...
 *  - PYCACHE/...
 *  - package-name/
 *  - package-name-1.2.3.dist-info/
 *  - ...
 */
data class CachedPackage(val name: PyPackageName, val version: PyPackageVersion, val repo: PyPackagesRepository, val srcPath: Path) {
    val descriptor = Requirements.Descriptor(name, version, repo.name)
    val dependencies = Dependencies()

    val sources: List<File>
        get() = srcPath.toFile().listFiles()?.toList() ?: emptyList()

    val infoDirectory: InstalledPackageInfo
        get() = InstalledPackageInfo.findByDescriptor(srcPath.toFile(), descriptor)

    override fun hashCode(): Int = srcPath.hashCode()

    class Dependencies {
        private val dependencies: MutableList<CachedPackage> = ArrayList()

        fun all(): Set<CachedPackage> {
            return dependencies.toSet()
        }

        fun register(dependency: CachedPackage) {
            dependencies.add(dependency)
        }
    }
}
