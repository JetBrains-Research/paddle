package io.paddle.plugin.python.dependencies

import io.paddle.plugin.python.Config
import io.paddle.plugin.python.extensions.Requirements
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

/**
 * A service class for managing Paddle's cache.
 *
 * @see Config.cacheDir
 */
object GlobalCacheRepository {
    private val cachedPackages: MutableList<CachedPackage> = mutableListOf()

    init {
        val cacheDir = Config.cacheDir.toFile()
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        cacheDir.listFiles()?.forEach { packageDir ->
            packageDir.listFiles()?.forEach { versionDir ->
                val descriptor = Requirements.Descriptor(name = packageDir.name, version = versionDir.name)
                cachedPackages.add(CachedPackage(descriptor, versionDir.toPath()))
            }
        }
    }

    fun hasCached(descriptor: Requirements.Descriptor) = cachedPackages.any { it.descriptor == descriptor }

    fun getPathToPackage(dependencyDescriptor: Requirements.Descriptor): Path =
        Config.cacheDir.resolve(dependencyDescriptor.name).resolve(dependencyDescriptor.version)

    fun findPackage(dependencyDescriptor: Requirements.Descriptor): CachedPackage {
        return if (!hasCached(dependencyDescriptor)) {
            installToCache(dependencyDescriptor)
        } else {
            cachedPackages.find { it.descriptor == dependencyDescriptor }!!
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
        try {
            packageSources.forEach { it.copyRecursively(target = targetPath.resolve(it.name).toFile(), overwrite = true) }
            val pkg = CachedPackage(dependencyDescriptor, srcPath = targetPath)

            // Copy dependent packages (resolved and installed by PIP for now) recursively
            for (dependencyName in pkg.metadata.requiresDist) {
                if (pkg.metadata.providesExtra.contains(dependencyName)) {
                    continue // TODO: read docs about it and find proofs
                }
                val dependencyVersion = GlobalVenvManager.getInstalledPackageVersionByName(dependencyName)
                    ?: error("Package $dependencyName (required by ${pkg.name}) is not installed.")
                if (cachedPackages.any { it.name == dependencyName && it.version == dependencyVersion }) {
                    continue
                }
                val dependentPkg = copyPackageRecursivelyFromGlobalVenv(Requirements.Descriptor(dependencyName, dependencyVersion))
                pkg.dependencies.register(dependentPkg)
            }

            cachedPackages.add(pkg)
            return pkg

        } catch (ex: IOException) {
            error("Some IO problems occurred during caching the installed ${dependencyDescriptor.name}-${dependencyDescriptor.version} package.")
        }
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
