package io.paddle.plugin.python.dependencies.lock.models

import kotlinx.serialization.Serializable

@Serializable
data class LockedPyDistribution(
    val filename: String,
    val hash: String
)
