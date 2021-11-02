package io.paddle.plugin.python.dependencies.index.distributions

import kotlinx.serialization.Serializable

@Serializable
sealed class PyDistributionInfo {
    abstract val name: String
    abstract val version: String
    abstract val buildTag: String?
    abstract val distributionFilename: String
    abstract val ext: String

    companion object {
        fun fromString(filename: String): PyDistributionInfo? {
            return ArchivePyDistributionInfo.fromString(filename) as PyDistributionInfo?
                ?: WheelPyDistributionInfo.fromString(filename) as PyDistributionInfo?
        }
    }
}
