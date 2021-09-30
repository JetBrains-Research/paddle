package io.paddle.plugin.python.dependencies.index

import kotlinx.serialization.Serializable

@Serializable
data class JsonPackageMetadataInfo(
    val info: JsonPackageMetadataGeneralInfo,
    val releases: Map<String, List<JsonPackageMetadataReleaseInfo>>
)
