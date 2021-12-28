package io.paddle.plugin.python.dependencies

import io.paddle.plugin.python.PaddlePyConfig
import io.paddle.plugin.python.dependencies.index.PyPackage
import io.paddle.plugin.python.dependencies.index.PyPackagesRepository
import io.paddle.plugin.python.utils.exists
import io.paddle.project.Project
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.schedule

/**
 * A service class for managing Paddle's cache.
 *
 * @see PaddlePyConfig.cacheDir
 */
object GlobalCacheRepository {
    private const val CACHE_SYNC_PERIOD_MS: Long = 60000L
    private val cachedPackages: MutableCollection<CachedPackage> = ConcurrentHashMap.newKeySet()

    init {
        Timer("CachedPackagesSynchronizer", true).schedule(delay = 0, period = CACHE_SYNC_PERIOD_MS) {
            cachedPackages.clear()
            PaddlePyConfig.cacheDir.toFile().listFiles()?.forEach { repoDir ->
                val repo = PyPackagesRepository.loadMetadata(repoDir.resolve("metadata.txt"))
                repoDir.listFiles()?.filter { it.isDirectory }?.forEach { packageDir ->
                    packageDir.listFiles()?.forEach { versionDir ->
                        cachedPackages.add(CachedPackage(packageDir.name, versionDir.name, repo, versionDir.toPath()))
                    }
                }
                for (pkg in cachedPackages) {
                    for (dependencySpec in pkg.metadata.requiresDist) {
                        val dependencyName = dependencySpec.nameReq().name().text
                        // TODO: implement dependency resolution
                        val dependency = cachedPackages.findLast { it.name == dependencyName } ?: continue
                        pkg.dependencies.register(dependency)
                    }
                }
            }
        }
    }

    private fun hasCached(pkg: PyPackage): Boolean {
        return cachedPackages.any { it.descriptor == pkg.descriptor && it.srcPath.exists() }
    }

    private fun getPathToPackage(pkg: PyPackage): Path =
        PaddlePyConfig.cacheDir
            .resolve(pkg.repo.cacheFileName)
            .resolve(pkg.name)
            .resolve(pkg.version)

    fun findPackage(pkg: PyPackage, project: Project): CachedPackage {
        return if (!hasCached(pkg)) {
            installToCache(pkg, project)
        } else {
            cachedPackages.find { it.descriptor == pkg.descriptor }!!
        }
    }

    private fun installToCache(pkg: PyPackage, project: Project): CachedPackage {
        return GlobalVenvManager.smartInstall(pkg, project).expose(
            onSuccess = { copyPackageRecursivelyFromGlobalVenv(pkg, project) },
            onFail = { error("Some conflict occurred during installation of ${pkg.name}.") }
        )
    }

    private fun copyPackageRecursivelyFromGlobalVenv(pkg: PyPackage, project: Project): CachedPackage {
        val packageSources = GlobalVenvManager.getPackageRelatedStuff(pkg.descriptor)
        val targetPath = getPathToPackage(pkg)
        packageSources.forEach { it.copyRecursively(target = targetPath.resolve(it.name).toFile(), overwrite = true) }
        val cachedPkg = CachedPackage(pkg.name, pkg.version, pkg.repo, srcPath = targetPath)

        for (dependencySpec in cachedPkg.metadata.requiresDist) {
            val dependencyName = dependencySpec.nameReq().name().text
            if (!GlobalVenvManager.contains(dependencyName)) {
                continue // assumption: PIP didn't install it => we don't need it
            }
            val dependencyVersion = GlobalVenvManager.getInstalledPackageVersionByName(dependencyName)
                ?: error("Package $dependencyName (required by ${cachedPkg.name}) is not installed.")

            // TODO: avoid distr-filename resolution
            val dependencyPkg = PyPackage.resolve(dependencyName, dependencyVersion, project)

            if (hasCached(dependencyPkg)) {
                continue
            }
            val dependentPkg = copyPackageRecursivelyFromGlobalVenv(dependencyPkg, project)
            cachedPkg.dependencies.register(dependentPkg)
        }

        cachedPackages.add(cachedPkg)
        return cachedPkg
    }

    fun createSymlinkToPackageRecursively(pkg: CachedPackage, symlinkDir: Path) {
        if (Files.notExists(symlinkDir.resolve(pkg.name))) {
            pkg.sources.forEach { Files.createSymbolicLink(symlinkDir.resolve(it.name), it.toPath()) }
            for (dependentPkg in pkg.dependencies.all()) {
                createSymlinkToPackageRecursively(dependentPkg, symlinkDir)
            }
        } else {
            // TODO: check versions compatibility?
        }
    }
}
