package io.paddle.plugin.python.dependencies

import com.intellij.util.io.exists
import io.paddle.plugin.python.Config
import io.paddle.plugin.python.dependencies.index.PyPackageRepositoryUrl
import io.paddle.plugin.python.extensions.Requirements
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
        Config.cacheDir.toFile().listFiles()?.forEach { packageDir ->
            packageDir.listFiles()?.forEach { versionDir ->
                val descriptor = Requirements.Descriptor(name = packageDir.name, version = versionDir.name)
                cachedPackages.add(CachedPackage(descriptor, versionDir.toPath()))
            }
        }
    }

    fun hasCached(descriptor: Requirements.Descriptor) = cachedPackages.any { it.descriptor == descriptor && it.srcPath.exists() }

    fun getPathToPackage(dependencyDescriptor: Requirements.Descriptor): Path =
        Config.cacheDir.resolve(dependencyDescriptor.name).resolve(dependencyDescriptor.version)

    fun findPackage(dependencyDescriptor: Requirements.Descriptor, repositories: List<PyPackageRepositoryUrl>): CachedPackage {
        return if (!hasCached(dependencyDescriptor)) {
            installToCache(dependencyDescriptor, repositories)
        } else {
            cachedPackages.find { it.descriptor == dependencyDescriptor }!!
        }
    }

    fun installToCache(dependencyDescriptor: Requirements.Descriptor, repositories: List<PyPackageRepositoryUrl>): CachedPackage {
        return GlobalVenvManager.smartInstall(dependencyDescriptor, repositories).expose(
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
