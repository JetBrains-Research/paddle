package io.paddle.plugin.python.dependencies

import io.paddle.plugin.python.PyLocations
import io.paddle.plugin.python.dependencies.packages.*
import io.paddle.plugin.python.utils.deepResolve
import io.paddle.plugin.python.utils.exists
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import java.io.File
import java.io.FileFilter
import java.nio.file.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.schedule
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
            repoDir.listFiles(FileFilter { it.isDirectory })?.forEach { packageDir ->
                packageDir.listFiles(FileFilter { it.isDirectory })?.forEach { versionDir ->
                    cachedPackages.add(CachedPyPackage.load(versionDir.toPath()))
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

    fun findOrInstallPackage(pkg: PyPackage, project: PaddleProject): CachedPyPackage {
        return cachedPackages.find { it.pkg == pkg && it.srcPath.exists() } ?: installToCache(pkg, project)
    }

    private fun installToCache(pkg: PyPackage, project: PaddleProject): CachedPyPackage {
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

    private fun copyPackageRecursivelyFromTempVenv(pkg: PyPackage, project: PaddleProject): CachedPyPackage {
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
        for (topLevelSourceDir in cachedPkg.topLevelSources) {
            when (topLevelSourceDir.name) {
                "BIN", "PYCACHE" -> {
                    topLevelSourceDir.walkBottomUp().forEach { cachedEntry ->
                        val link = venv.bin.resolve(cachedEntry.relativeTo(topLevelSourceDir).path)
                        if (link.exists()) {
                            when {
                                link.isDirectory -> return@forEach
                                link.isFile -> link.delete()
                            }
                        } else {
                            link.parentFile.mkdirs()
                        }
                        Files.createSymbolicLink(link.toPath(), cachedEntry.toPath())
                    }
                }

                else -> {
                    topLevelSourceDir.walkBottomUp().forEach { cachedEntry ->
                        val link = venv.sitePackages.resolve(cachedEntry.relativeTo(topLevelSourceDir.parentFile).path)
                        if (link.exists()) {
                            when {
                                link.isDirectory -> return@forEach
                                link.isFile -> link.delete()
                            }
                        } else {
                            link.parentFile.mkdirs()
                        }
                        Files.createSymbolicLink(link.toPath(), cachedEntry.toPath())
                    }
                }
            }
        }
    }
}
