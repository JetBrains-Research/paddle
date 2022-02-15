package io.paddle.plugin.python.dependencies.packages

import io.paddle.plugin.python.dependencies.InstalledPackageInfoDir
import io.paddle.plugin.python.utils.jsonParser
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.nio.file.Path
import kotlin.io.path.readText

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
data class CachedPyPackage(val pkg: PyPackage, val srcPath: Path) : IResolvedPyPackage {
    override val name = pkg.name
    override val version = pkg.version
    override val repo = pkg.repo // FIXME: different repos in single project???
    override val distributionUrl = pkg.distributionUrl

    companion object {
        const val PYPACKAGE_CACHE_FILENAME = "PyPackage.json"

        fun load(srcPath: Path): CachedPyPackage {
            val pkg = jsonParser.decodeFromString<PyPackage>(srcPath.resolve(PYPACKAGE_CACHE_FILENAME).readText())
            return CachedPyPackage(pkg, srcPath)
        }
    }

    init {
        srcPath.resolve(PYPACKAGE_CACHE_FILENAME).toFile().writeText(jsonParser.encodeToString(pkg))
    }

    val sources: List<File>
        get() = srcPath.toFile().listFiles()?.toList() ?: emptyList()

    val infoDirectory: InstalledPackageInfoDir
        get() = InstalledPackageInfoDir.findByNameAndVersion(srcPath.toFile(), pkg.name, pkg.version)
}
