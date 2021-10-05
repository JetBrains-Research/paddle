package io.paddle.plugin.python.dependencies

import io.paddle.plugin.python.extensions.Requirements
import io.paddle.utils.StringHashable
import java.io.File
import java.nio.file.Path

data class CachedPackage(val descriptor: Requirements.Descriptor, val srcPath: Path) {
    val name: String = descriptor.name
    val version: String = descriptor.version

    val sources: List<File> = srcPath.toFile().listFiles()?.toList() ?: emptyList()
    val distInfo: File = srcPath.toFile().resolve(descriptor.distInfoDirName)
    val dependencies = Dependencies()

    val metadata: PackageMetadata by lazy {
        PackageMetadata.parse(distInfo.resolve("METADATA"))
    }

    val topLevelName: String by lazy {
        distInfo.resolve("top_level.txt").let { if (it.exists()) it.readText().trim() else name }
    }

    override fun hashCode(): Int {
        return StringHashable("$name:$version").hash().hashCode()
    }
}
