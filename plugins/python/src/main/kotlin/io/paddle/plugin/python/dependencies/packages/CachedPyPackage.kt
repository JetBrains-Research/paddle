package io.paddle.plugin.python.dependencies.packages

import io.paddle.plugin.python.dependencies.InstalledPackageInfoDir
import io.paddle.tasks.Task
import java.io.File
import java.nio.file.Path

/**
 * Decorator: cached version of PyPackage. Stored in the global cache folder.
 *
 * Common structure:
 *  - BIN/...
 *  - PYCACHE/...
 *  - package-name/
 *  - package-name-1.2.3.dist-info/
 *      - PyPackage.json
 *      - METADATA
 *      - ...
 *  - ...
 */
data class CachedPyPackage(val pkg: PyPackage, val srcPath: Path) : IResolvedPyPackage {
    override val name = pkg.name
    override val version = pkg.version
    override val repo = pkg.repo
    override val distributionUrl = pkg.distributionUrl

    companion object {
        const val PYPACKAGE_CACHE_FILENAME = "PyPackage.json"

        fun load(srcPath: Path): CachedPyPackage {
            val infoDir = InstalledPackageInfoDir.findIfSingle(srcPath.toFile())
            val pkg = infoDir.pkg ?: throw Task.ActException("Failed to load cached package from $srcPath: 'PyPackage.json' not found.'")
            return CachedPyPackage(pkg, srcPath)
        }
    }

    /**
     * E.g., listOf("BIN", "PYCACHE", "package_name", "package_name-1.2.3.dist-info")
     */
    val topLevelSources: List<File>
        get() = srcPath.toFile().listFiles()?.toList() ?: emptyList()
}
