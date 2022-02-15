package io.paddle.plugin.python.dependencies.packages

import io.paddle.plugin.python.dependencies.index.PyPackageRepoMetadataSerializer
import io.paddle.plugin.python.dependencies.index.PyPackagesRepository
import io.paddle.plugin.python.utils.*
import kotlinx.serialization.Serializable

/**
 * Resolved version of `Requirements.Descriptor`
 */
@Serializable
class PyPackage(
    override val name: PyPackageName,
    override val version: PyPackageVersion,
    @Serializable(with = PyPackageRepoMetadataSerializer::class) override val repo: PyPackagesRepository,
    override val distributionUrl: PyPackageUrl,
    var comesFrom: PyPackage? = null
) : ResolvedPyPackage {
    override fun hashCode(): Int {
        if (distributionUrl.contains('#')) {
            // to avoid hashes at the end, like ...#sha256=...
            return distributionUrl.substringBeforeLast('#').hashCode()
        }
        return distributionUrl.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PyPackage

        if (name != other.name) return false
        if (version != other.version) return false
        if (repo != other.repo) return false

        if (distributionUrl.contains('#')
            && other.distributionUrl.contains('#')
            && distributionUrl != other.distributionUrl
        ) return false

        return true
    }
}
