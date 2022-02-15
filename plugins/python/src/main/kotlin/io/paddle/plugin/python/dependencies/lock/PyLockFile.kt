package io.paddle.plugin.python.dependencies.lock

import io.paddle.plugin.python.dependencies.index.PyPackagesRepositoryIndexer
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.utils.jsonParser
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Path

@Serializable
class PyLockFile {
    companion object {
        const val FILENAME = "paddle-lock.json"

        fun fromFile(file: File): PyLockFile {
            return jsonParser.decodeFromString(file.readText())
        }
    }

    private val _lockedPackages = HashSet<LockedPyPackage>()

    val lockedPackages: Set<LockedPyPackage>
        get() = _lockedPackages.map { it.copy() }.toSet()

    suspend fun addLockedPackage(pkg: PyPackage) {
        val metadata = PyPackagesRepositoryIndexer.downloadMetadata(pkg)
        val distributions = metadata.releases[pkg.version] ?: error("Distribution $pkg was not found in metadata.")
        _lockedPackages.add(
            LockedPyPackage(
                LockedPyPackageIdentifier(pkg),
                comesFrom = pkg.comesFrom?.let { LockedPyPackageIdentifier(it) },
                distributions = distributions.map { LockedPyDistribution(it.filename, it.packageHash) }
            )
        )
    }

    fun save(path: Path) {
        val json = Json { prettyPrint = true }
        path.resolve(FILENAME).toFile().writeText(json.encodeToString(this))
    }
}
