package io.paddle.plugin.python.dependencies

import io.paddle.plugin.python.dependencies.index.PyPackagesRepository
import io.paddle.plugin.python.extensions.Requirements
import io.paddle.plugin.python.utils.PyPackageName
import io.paddle.plugin.python.utils.PyPackageVersion
import io.paddle.utils.StringHashable
import java.io.File
import java.nio.file.Path

data class CachedPackage(val name: PyPackageName, val version: PyPackageVersion, val repo: PyPackagesRepository, val srcPath: Path) {
    val descriptor = Requirements.Descriptor(name, version, repo.name)
    val sources: List<File> = srcPath.toFile().listFiles()?.toList() ?: emptyList()
    val distInfo: File = srcPath.toFile().resolve(descriptor.distInfoDirName)
    val dependencies = Dependencies()

    private val hashCode by lazy {
        StringHashable("$name:$version").hash().hashCode()
    }

    val metadata: CachedPackageMetadata by lazy {
        CachedPackageMetadata.parse(distInfo.resolve("METADATA"))
    }

    val topLevelName: String by lazy {
        distInfo.resolve("top_level.txt").let { if (it.exists()) it.readText().trim() else name }
    }

    override fun hashCode(): Int = srcPath.hashCode()

    class Dependencies {
        private val dependencies: MutableList<CachedPackage> = ArrayList()

        fun all(): Set<CachedPackage> {
            return dependencies.toSet()
        }

        fun register(dependency: CachedPackage) {
            dependencies.add(dependency)
        }
    }
}
