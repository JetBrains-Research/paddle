package io.paddle.plugin.python.dependencies.packages

import io.paddle.plugin.python.dependencies.InstalledPackageInfoDir
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
            val pkg = infoDir.pkg ?: error("Failed to load cached package from $srcPath: 'PyPackage.json' not found.'")
            return CachedPyPackage(pkg, srcPath)
        }
    }

    val sources: List<File>
        get() = srcPath.toFile().listFiles()?.toList() ?: emptyList()
}
