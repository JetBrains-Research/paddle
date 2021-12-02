package io.paddle.plugin.python.dependencies

import io.paddle.plugin.python.dependencies.index.PyDistributionsResolver
import io.paddle.plugin.python.dependencies.index.PyPackagesRepositories
import io.paddle.plugin.python.dependencies.index.PyPackagesRepository
import io.paddle.plugin.python.extensions.Requirements
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.schedule

/**
 * A service class for managing Paddle's cache.
 *
 * @see PythonDependenciesConfig.cacheDir
 */
object GlobalCacheRepository {
    private const val CACHE_SYNC_PERIOD_MS: Long = 60000L
    private val cachedPackages: MutableCollection<CachedPackage> = ConcurrentHashMap.newKeySet()

    init {
        Timer("CachedPackagesSynchronizer", true).schedule(delay = 0, period = CACHE_SYNC_PERIOD_MS) {
            synchronized(cachedPackages) {
                cachedPackages.clear()
                PythonDependenciesConfig.cacheDir.toFile().listFiles()?.forEach { repoDir ->
                    val repo = PyPackagesRepository.loadMetadata(repoDir.resolve("metadata.txt"))
                    repoDir.listFiles()?.filter { it.isDirectory }?.forEach { packageDir ->
                        packageDir.listFiles()?.forEach { versionDir ->
                            val descriptor = Requirements.Descriptor.resolve(packageDir.name, versionDir.name, repo)
                            cachedPackages.add(CachedPackage(descriptor, versionDir.toPath()))
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
    }

    private fun hasCached(descriptor: Requirements.Descriptor): Boolean {
        return cachedPackages.any { it.descriptor == descriptor && it.srcPath.exists() }
    }

    private fun getPathToPackage(descriptor: Requirements.Descriptor): Path =
        PythonDependenciesConfig.cacheDir
            .resolve(descriptor.repo.cacheFileName)
            .resolve(descriptor.name)
            .resolve(descriptor.version)

    fun findPackage(descriptor: Requirements.Descriptor, repositories: PyPackagesRepositories): CachedPackage {
        return if (!hasCached(descriptor)) {
            installToCache(descriptor, repositories)
        } else {
            cachedPackages.find { it.descriptor == descriptor }!!
        }
    }

    private fun installToCache(descriptor: Requirements.Descriptor, repositories: PyPackagesRepositories): CachedPackage {
        return GlobalVenvManager.smartInstall(descriptor, repositories).expose(
            onSuccess = { copyPackageRecursivelyFromGlobalVenv(descriptor, repositories) },
            onFail = { error("Some conflict occurred during installation of ${descriptor.name}.") }
        )
    }

    private fun copyPackageRecursivelyFromGlobalVenv(descriptor: Requirements.Descriptor, repositories: PyPackagesRepositories): CachedPackage {
        val packageSources = GlobalVenvManager.getPackageRelatedStuff(descriptor)
        val targetPath = getPathToPackage(descriptor)
        packageSources.forEach { it.copyRecursively(target = targetPath.resolve(it.name).toFile(), overwrite = true) }
        val pkg = CachedPackage(descriptor, srcPath = targetPath)

        for (dependencySpec in pkg.metadata.requiresDist) {
            val dependencyName = dependencySpec.nameReq().name().text
            if (!GlobalVenvManager.contains(dependencyName)) {
                continue // assumption: PIP didn't install it => we don't need it
            }
            val dependencyVersion = GlobalVenvManager.getInstalledPackageVersionByName(dependencyName)
                ?: error("Package $dependencyName (required by ${pkg.name}) is not installed.")
            val dependencyUrl = PyDistributionsResolver.resolve(dependencyName, dependencyVersion, repositories)
            val dependencyRepo = repositories.getRepositoryByPyPackageUrl(dependencyUrl)
            val dependencyDescriptor = Requirements.Descriptor.resolve(dependencyName, dependencyVersion, dependencyRepo)
            if (hasCached(dependencyDescriptor)) {
                continue
            }
            val dependentPkg = copyPackageRecursivelyFromGlobalVenv(dependencyDescriptor, repositories)
            pkg.dependencies.register(dependentPkg)
        }

        cachedPackages.add(pkg)
        return pkg
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
