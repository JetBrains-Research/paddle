package io.paddle.plugin.python.dependencies

import io.paddle.plugin.python.extensions.Requirements
import io.paddle.utils.exists
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.concurrent.schedule

/**
 * A service class for managing Paddle's cache.
 *
 * @see PythonDependenciesConfig.cacheDir
 */
object GlobalCacheRepository {
    private const val CACHE_SYNC_PERIOD_MS: Long = 60000L
    private val cachedPackages: MutableCollection<CachedPackage> = HashSet()

    init {
        Timer("CachedPackagesSynchronizer", true).schedule(delay = 0, period = CACHE_SYNC_PERIOD_MS) {
            synchronized(cachedPackages) {
                cachedPackages.clear()
                PythonDependenciesConfig.cacheDir.toFile().listFiles()?.forEach { packageDir ->
                    packageDir.listFiles()?.forEach { versionDir ->
                        val descriptor = Requirements.Descriptor(name = packageDir.name, version = versionDir.name)
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

    fun hasCached(descriptor: Requirements.Descriptor) = synchronized(cachedPackages) {
        cachedPackages.any { it.descriptor == descriptor && it.srcPath.exists() }
    }

    fun getPathToPackage(dependencyDescriptor: Requirements.Descriptor): Path =
        PythonDependenciesConfig.cacheDir.resolve(dependencyDescriptor.name).resolve(dependencyDescriptor.version)

    fun findPackage(dependencyDescriptor: Requirements.Descriptor): CachedPackage {
        return if (!hasCached(dependencyDescriptor)) {
            installToCache(dependencyDescriptor)
        } else {
            synchronized(cachedPackages) { cachedPackages.find { it.descriptor == dependencyDescriptor }!! }
        }
    }

    fun installToCache(dependencyDescriptor: Requirements.Descriptor): CachedPackage {
        return GlobalVenvManager.smartInstall(dependencyDescriptor).expose(
            onSuccess = { copyPackageRecursivelyFromGlobalVenv(dependencyDescriptor) },
            onFail = { error("Some conflict occurred during installation of ${dependencyDescriptor.name}.") }
        )
    }

    private fun copyPackageRecursivelyFromGlobalVenv(dependencyDescriptor: Requirements.Descriptor): CachedPackage {
        val packageSources = GlobalVenvManager.getPackageRelatedStuff(dependencyDescriptor)
        val targetPath = getPathToPackage(dependencyDescriptor)
        packageSources.forEach { it.copyRecursively(target = targetPath.resolve(it.name).toFile(), overwrite = true) }
        val pkg = CachedPackage(dependencyDescriptor, srcPath = targetPath)

        for (dependencySpec in pkg.metadata.requiresDist) {
            val dependencyName = dependencySpec.nameReq().name().text
            if (!GlobalVenvManager.contains(dependencyName)) {
                continue // assumption: PIP didn't install it => we don't need it
            }
            val dependencyVersion = GlobalVenvManager.getInstalledPackageVersionByName(dependencyName)
                ?: error("Package $dependencyName (required by ${pkg.name}) is not installed.")
            if (hasCached(Requirements.Descriptor(dependencyName, dependencyVersion))) {
                continue
            }
            val dependentPkg = copyPackageRecursivelyFromGlobalVenv(Requirements.Descriptor(dependencyName, dependencyVersion))
            pkg.dependencies.register(dependentPkg)
        }

        synchronized(cachedPackages) { cachedPackages.add(pkg) }
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
