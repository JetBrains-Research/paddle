package io.paddle.plugin.python.dependencies

import io.paddle.plugin.python.PyLocations
import io.paddle.plugin.python.dependencies.packages.*
import io.paddle.plugin.python.utils.deepResolve
import io.paddle.plugin.python.utils.exists
import io.paddle.project.Project
import io.paddle.tasks.Task
import java.io.File
import java.nio.file.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.schedule
import kotlin.io.path.deleteExisting
import kotlin.io.path.name

/**
 * A service class for managing Paddle's cache.
 *
 * @see PyLocations.packagesDir
 */
object GlobalCacheRepository {
    private const val CACHE_SYNC_PERIOD_MS: Long = 60000L
    private val cachedPackages: MutableCollection<CachedPyPackage> = ConcurrentHashMap.newKeySet()

    init {
        Timer("CachedPackagesSynchronizer", true)
            .schedule(delay = 0, period = CACHE_SYNC_PERIOD_MS) { updateCache() }
    }

    fun updateCache() {
        cachedPackages.clear()
        PyLocations.packagesDir.toFile().listFiles()?.forEach { repoDir ->
            repoDir.listFiles()?.forEach { packageDir ->
                packageDir.listFiles()?.forEach { srcPath ->
                    cachedPackages.add(CachedPyPackage.load(srcPath.toPath()))
                }
            }
        }
    }

    private fun getPathToCachedPackage(pkg: PyPackage): Path =
        PyLocations.packagesDir.deepResolve(
            pkg.repo.cacheFileName,
            pkg.name,
            pkg.version
        )

    fun findPackage(pkg: PyPackage, project: Project): CachedPyPackage {
        return cachedPackages.find { it.pkg == pkg && it.srcPath.exists() }
            ?: installToCache(pkg, project)
    }

    private fun installToCache(pkg: PyPackage, project: Project): CachedPyPackage {
        val tempVenvManager = TempVenvManager.getInstance(project)
        return tempVenvManager.install(pkg).expose(
            onSuccess = {
                copyPackageRecursivelyFromTempVenv(pkg, project).also {
                    tempVenvManager.uninstall(pkg)
                }
            },
            onFail = { throw Task.ActException("Some conflict occurred during installation of ${pkg.name}.") }
        )
    }

    private fun copyPackageRecursivelyFromTempVenv(pkg: PyPackage, project: Project): CachedPyPackage {
        val tmpVenvManager = TempVenvManager.getInstance(project)
        val targetPathToCache = getPathToCachedPackage(pkg)

        copyPackageSourcesFromTempVenv(tmpVenvManager, pkg, targetPathToCache)

        val cachedPkg = CachedPyPackage(pkg, srcPath = targetPathToCache)
        cachedPackages.add(cachedPkg)

        return cachedPkg
    }

    private fun copyPackageSourcesFromTempVenv(venvManager: TempVenvManager, pkg: IResolvedPyPackage, targetPathToCache: Path) {
        val packageSources = venvManager.getFilesRelatedToPackage(pkg)
        val sep = File.separatorChar
        packageSources.forEach {
            val commonPathBin = Paths.get(it.path.commonPrefixWith(venvManager.venv.bin.path).trim(sep))
            val commonPathPyCache = Paths.get(it.path.commonPrefixWith(venvManager.venv.pycache.path).trim(sep))
            val targetPathToFile = when {
                commonPathBin.name == "bin" -> {
                    // If common path prefix ends with "bin", copy it to "BIN" folder
                    val suffix = it.path.substringAfter(venvManager.venv.bin.path).trim(sep)
                    targetPathToCache.resolve("BIN").deepResolve(*suffix.split(sep).toTypedArray()).toFile()
                }
                commonPathPyCache.name == "__pycache__" -> {
                    // If common path prefix ends with "__pycache__", copy it to "PYCACHE" folder
                    val suffix = it.path.substringAfter(venvManager.venv.pycache.path).trim(sep)
                    targetPathToCache.resolve("PYCACHE").deepResolve(*suffix.split(sep).toTypedArray()).toFile()
                }
                else -> {
                    // Otherwise, it is general file from site-packages folder which should be copied as is
                    val suffix = it.path.substringAfter(venvManager.venv.sitePackages.path).trim(sep)
                    targetPathToCache.deepResolve(*suffix.split(sep).toTypedArray()).toFile()
                }
            }
            targetPathToFile.parentFile.mkdirs()
            it.copyRecursively(targetPathToFile, overwrite = true)
        }
    }

    fun createSymlinkToPackage(cachedPkg: CachedPyPackage, venv: VenvDir) {
        cachedPkg.sources.forEach { src ->
            when (src.name) {
                "BIN", "PYCACHE" -> {
                    src.listFiles()?.forEach { file ->
                        val link = venv.bin.resolve(file.name).also { it.mkdirs() }
                        if (link.exists()) {
                            link.delete()
                        }
                        Files.createSymbolicLink(link.toPath(), file.toPath())
                    }
                }
                else -> {
                    val link = venv.sitePackages.resolve(src.name).toPath()
                    if (link.exists()) {
                        link.deleteExisting()
                    }
                    Files.createSymbolicLink(link, src.toPath())
                }
            }
        }
    }
}
