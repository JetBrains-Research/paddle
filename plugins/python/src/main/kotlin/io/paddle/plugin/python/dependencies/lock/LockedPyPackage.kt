package io.paddle.plugin.python.dependencies.lock

import io.paddle.plugin.python.dependencies.index.PyPackagesRepository
import io.paddle.plugin.python.utils.PyPackageUrl
import io.paddle.project.Project
import kotlinx.serialization.Serializable

@Serializable
data class LockedPyPackage(
    val identifier: LockedPyPackageIdentifier,
    val comesFrom: LockedPyPackageIdentifier?,
    val distributions: List<LockedPyDistribution>
) {
    val name = identifier.name
    val version = identifier.version
    val repoMetadata = identifier.repoMetadata

    suspend fun resolveConcreteDistribution(repo: PyPackagesRepository, project: Project): PyPackageUrl {
        return identifier.resolveConcreteDistribution(repo, project)
    }
}
