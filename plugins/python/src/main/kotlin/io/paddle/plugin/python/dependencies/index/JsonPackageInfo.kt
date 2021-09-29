package io.paddle.plugin.python.dependencies.index

import kotlinx.serialization.Serializable

@Serializable
data class JsonPackageInfo(
    val info: JsonPackageGeneralInfo,
    val releases: Map<String, List<JsonPackageReleaseInfo>>
)
