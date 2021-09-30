package io.paddle.plugin.python.dependencies.index.distributions

import kotlinx.serialization.Serializable

@Serializable
sealed class PyDistributionInfo {
    abstract val version: String
    abstract val buildTag: String?
    abstract val distributionFilename: String
}
