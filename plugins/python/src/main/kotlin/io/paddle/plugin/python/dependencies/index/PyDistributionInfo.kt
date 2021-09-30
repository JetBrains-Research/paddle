package io.paddle.plugin.python.dependencies.index

import kotlinx.serialization.Serializable

@Serializable
sealed class PyDistributionInfo {
    abstract val version: String
    abstract val buildTag: String?
    abstract val distrFilename: String
}
