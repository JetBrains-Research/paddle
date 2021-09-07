package io.paddle.plugin.python.dependencies

import io.paddle.plugin.python.Config
import io.paddle.plugin.python.extensions.Requirements
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

    fun install(dependency: Requirements.Descriptor) = GlobalVenvManager.install(dependency)
}
