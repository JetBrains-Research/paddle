package io.paddle.plugin.python.dependencies.lock

import io.paddle.plugin.python.dependencies.index.PyPackage
import io.paddle.plugin.python.dependencies.index.metadata.JsonPackageMetadataInfo
import io.paddle.plugin.python.dependencies.lock.hash.HashUtils
import io.paddle.plugin.python.dependencies.lock.hash.MessageDigestAlgorithm
import io.paddle.plugin.python.utils.PyPackagesRepositoryUrl
import io.paddle.plugin.python.utils.jsonParser
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.File
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
    val repoUrl: PyPackagesRepositoryUrl,
    val repoName: String,
    val distributions: List<LockedPyDistribution>
)

@Serializable
class PyLockFile {
    companion object {
        const val FILENAME = "paddle-lock.json"

        fun fromFile(file: File): PyLockFile {
            return jsonParser.decodeFromString(file.readText())
        }
    }

    private val lockedPyPackagesData = HashSet<LockedPyPackage>()
    private lateinit var contentHash: String

    val packages: Set<LockedPyPackage>
        get() = lockedPyPackagesData.map { it.copy() }.toSet()

    fun addLockedPackage(pkg: PyPackage, metadata: JsonPackageMetadataInfo) {
        val distributions = metadata.releases[pkg.version] ?: error("Distribution $pkg was not found in metadata.")
        lockedPyPackagesData.add(
            LockedPyPackage(
                name = pkg.name,
                version = pkg.version,
                repoUrl = pkg.repo.url,
                repoName = pkg.repo.name,
                distributions = distributions.map { LockedPyDistribution(it.filename, it.packageHash) }
            )
        )
    }

    fun save(path: Path) {
        contentHash = HashUtils.getCheckSumFromString(
            digest = MessageDigest.getInstance(MessageDigestAlgorithm.SHA_256),
            src = lockedPyPackagesData.sortedBy { it.name }.toString()
        )
        val json = Json { prettyPrint = true }
        path.resolve(FILENAME).toFile().writeText(json.encodeToString(this))
    }
}
