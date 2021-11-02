package io.paddle.plugin.python.dependencies.lock

import io.paddle.plugin.python.dependencies.index.PyPackagesRepository
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.index.utils.*
import io.paddle.plugin.python.dependencies.index.utils.jsonParser
import io.paddle.plugin.python.dependencies.lock.hash.HashUtils
import io.paddle.plugin.python.dependencies.lock.hash.MessageDigestAlgorithm
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.nio.file.Path
import java.security.MessageDigest

@Serializable
data class LockedPyPackage(
    val name: String,
    val version: String,
    val distributionFilename: String,
    val repositoryUrl: PyPackagesRepositoryUrl,
    val hash: String
)

@Serializable
class PyLockFile {
    companion object {
        const val FILENAME = "paddle.lock"
    }

    private val lockedPyPackagesData = HashSet<LockedPyPackage>()
    private lateinit var contentHash: String

    fun addLockedPackage(distributionInfo: PyDistributionInfo, repository: PyPackagesRepository, distributionHash: String) {
        lockedPyPackagesData.add(
            LockedPyPackage(
                name = distributionInfo.name,
                version = distributionInfo.version,
                distributionFilename = distributionInfo.distributionFilename,
                repositoryUrl = repository.urlSimple,
                hash = distributionHash
            )
        )
    }

    fun save(path: Path) {
        contentHash = HashUtils.getCheckSumFromString(
            digest = MessageDigest.getInstance(MessageDigestAlgorithm.SHA_256),
            src = lockedPyPackagesData.toString()
        )
        path.resolve(FILENAME).toFile().writeText(jsonParser.encodeToString(this))
    }
}
