package io.paddle.plugin.python.dependencies.lock

import io.paddle.plugin.python.dependencies.index.metadata.JsonPackageMetadataInfo
import io.paddle.plugin.python.dependencies.index.utils.PyPackagesRepositoryUrl
import io.paddle.plugin.python.dependencies.index.utils.jsonParser
import io.paddle.plugin.python.dependencies.lock.hash.HashUtils
import io.paddle.plugin.python.dependencies.lock.hash.MessageDigestAlgorithm
import io.paddle.plugin.python.extensions.Requirements
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.nio.file.Path
import java.security.MessageDigest

@Serializable
data class LockedPyDistribution(
    val filename: String,
    val hash: String
)

@Serializable
data class LockedPyPackage(
    val name: String,
    val version: String,
    val repositoryUrl: PyPackagesRepositoryUrl,
    val distributions: List<LockedPyDistribution>
)

@Serializable
class PyLockFile {
    companion object {
        const val FILENAME = "paddle.lock"
    }

    private val lockedPyPackagesData = HashSet<LockedPyPackage>()
    private lateinit var contentHash: String

    fun addLockedPackage(descriptor: Requirements.Descriptor, metadata: JsonPackageMetadataInfo) {
        val distributions = metadata.releases[descriptor.version] ?: error("Distribution $descriptor was not found in metadata.")
        lockedPyPackagesData.add(
            LockedPyPackage(
                name = descriptor.name,
                version = descriptor.version,
                repositoryUrl = descriptor.repo.urlSimple,
                distributions = distributions.map { LockedPyDistribution(it.filename, it.getPackageHash()) }
            )
        )
    }

    fun save(path: Path) {
        contentHash = HashUtils.getCheckSumFromString(
            digest = MessageDigest.getInstance(MessageDigestAlgorithm.SHA_256),
            src = lockedPyPackagesData.sortedBy { it.name }.toString()
        )
        path.resolve(FILENAME).toFile().writeText(jsonParser.encodeToString(this))
    }
}
