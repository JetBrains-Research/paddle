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

    fun getPathToDependency(dependency: Requirements.Descriptor): Path = Config.cacheDir.resolve(dependency.name).resolve(dependency.version)

    fun installToCache(dependency: Requirements.Descriptor) {
        val code = GlobalVenvManager.install(dependency)
        if (code == 0) {
            val packageToCopy = GlobalVenvManager.globalVenv.sitePackages.resolve(dependency.name)
            try {
                packageToCopy.copyRecursively(
                    target = getPathToDependency(dependency).toFile(),
                    overwrite = true
                )
            } catch (ex: IOException) {
                error("Some IO problems occurred during caching the installed ${dependency.name}-${dependency.version} package.")
            }
        } else {
            TODO("Conflict occurred. Clear globalVenv and try again")
        }
    }

    fun createSymlinkToPackage(dependency: Requirements.Descriptor, linkPath: Path) {
        if (!Files.exists(linkPath)) {
            Files.createSymbolicLink(linkPath, getPathToDependency(dependency))
        } else {
            error("The specified package is already installed.") // TODO: custom exceptions?
        }
    }
}
