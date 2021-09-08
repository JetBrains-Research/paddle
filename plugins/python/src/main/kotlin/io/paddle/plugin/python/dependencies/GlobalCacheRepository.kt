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

    fun getPathToPackageDistInfo(dependency: Requirements.Descriptor): Path =
        Config.cacheDir.resolve(dependency.name).resolve(dependency.distInfoDirName)

    fun installToCache(dependency: Requirements.Descriptor) {
        val code = GlobalVenvManager.install(dependency)
        if (code == 0) {
            copyFromGlobalVenv(dependency)
        } else {
            TODO("Conflict occurred. Clear globalVenv and try again")
        }
    }

    private fun copyFromGlobalVenv(dependency: Requirements.Descriptor) {
        val packageToCopy = GlobalVenvManager.globalVenv.sitePackages.resolve(dependency.name)
        val packageDistInfoToCopy = GlobalVenvManager.globalVenv.sitePackages.resolve(dependency.distInfoDirName)
        try {
            packageToCopy.copyRecursively(target = getPathToPackage(dependency).toFile(), overwrite = true)
            packageDistInfoToCopy.copyRecursively(target = getPathToPackageDistInfo(dependency).toFile(), overwrite = true)
        } catch (ex: IOException) {
            error("Some IO problems occurred during caching the installed ${dependency.name}-${dependency.version} package.")
        }
    }

    fun createSymlinkToPackage(dependency: Requirements.Descriptor, linkParentDir: Path) {
        val packageLinkPath = linkParentDir.resolve(dependency.name)
        val packageDistInfoLinkPath = linkParentDir.resolve(dependency.distInfoDirName)
        if (Files.notExists(packageLinkPath) && Files.notExists(packageDistInfoLinkPath)) {
            Files.createSymbolicLink(packageLinkPath, getPathToPackage(dependency))
            Files.createSymbolicLink(packageDistInfoLinkPath, getPathToPackageDistInfo(dependency))
        } else {
            error("The specified package <${dependency.name}> is already installed, or removed incorrectly.") // TODO: custom exceptions?
        }
    }
}
