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
    private val cachedPackages: MutableList<Requirements.Descriptor> = mutableListOf()

    init {
        val cacheDir = Config.cacheDir.toFile()
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }

        cacheDir.listFiles()?.forEach { packageDir ->
            packageDir.listFiles()?.forEach { versionDir ->
                cachedPackages.add(Requirements.Descriptor(name = packageDir.name, version = versionDir.name))
            }
        }
    }

    fun hasCached(dependency: Requirements.Descriptor) = cachedPackages.contains(dependency)

    fun getPathToPackage(dependency: Requirements.Descriptor): Path =
        Config.cacheDir.resolve(dependency.name).resolve(dependency.version)

    fun installToCache(dependency: Requirements.Descriptor) {
        val code = GlobalVenvManager.install(dependency)
        if (code == 0) {
            copyPackageRecursivelyFromGlobalVenv(dependency)
        } else {
            TODO("Conflict occurred. Clear globalVenv and try again")
        }
    }

    private fun copyPackageRecursivelyFromGlobalVenv(dependency: Requirements.Descriptor) {
        val packageSources = GlobalVenvManager.getPackageRelatedStuff(dependency)
        try {
            packageSources.forEach { it.copyRecursively(target = getPathToPackage(dependency).resolve(it.name).toFile(), overwrite = true) }
            cachedPackages.add(dependency)
            copyDependentPackagesFromGlobalVenv(dependency)
        } catch (ex: IOException) {
            error("Some IO problems occurred during caching the installed ${dependency.name}-${dependency.version} package.")
        }
    }

    private fun copyDependentPackagesFromGlobalVenv(parentDependency: Requirements.Descriptor) {
        val distInfoDir = GlobalVenvManager.globalVenv.sitePackages.resolve(parentDependency.distInfoDirName)
        val metadata = PackageMetadata.parse(distInfoDir.resolve("METADATA"))
        for (packageName in metadata.requiresDist) {
            if (metadata.providesExtra.contains(packageName)) {
                continue
            }
            if (cachedPackages.any { it.name.startsWith(packageName) }) {
                continue // FIXME: consider versions as well
            }
            // Pip has already installed the required dependent packages, so we should only find it within the venv
            val version = GlobalVenvManager.getInstalledPackageVersionByName(packageName)
                ?: error("Package $packageName (required by ${parentDependency.name}) is not installed.")

            val childDependency = Requirements.Descriptor(packageName, version)
            copyPackageRecursivelyFromGlobalVenv(childDependency)
        }
    }

    fun createSymlinkToPackageRecursively(dependency: Requirements.Descriptor, parentDirOfSymlink: Path) {
        if (Files.notExists(parentDirOfSymlink.resolve(dependency.name))) {
            getPathToPackage(dependency).toFile().listFiles()?.forEach {
                Files.createSymbolicLink(parentDirOfSymlink.resolve(it.name), it.toPath())
            }
            createSymLinksToDependentPackagesRecursively(dependency, parentDirOfSymlink)
        } else {
            // TODO: check versions compatibility?
        }
    }

    private fun createSymLinksToDependentPackagesRecursively(parentDependency: Requirements.Descriptor, parentDirOfSymlink: Path) {
        val distInfoDir = GlobalVenvManager.globalVenv.sitePackages.resolve(parentDependency.distInfoDirName)
        val metadata = PackageMetadata.parse(distInfoDir.resolve("METADATA"))
        for (packageName in metadata.requiresDist) {
            if (metadata.providesExtra.contains(packageName)) {
                continue
            }
            val version = GlobalVenvManager.getInstalledPackageVersionByName(packageName)
                ?: error("Package $packageName (required by ${parentDependency.name} is not installed.")

            val childDependency = Requirements.Descriptor(packageName, version)
            createSymlinkToPackageRecursively(childDependency, parentDirOfSymlink)
        }
    }
}
