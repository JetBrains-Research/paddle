package io.paddle.plugin.python.dependencies.index.metadata

import io.paddle.plugin.python.dependencies.packages.PyPackageVersion
import kotlinx.serialization.Serializable

@Serializable
data class JsonPackageMetadataInfo(
    val info: JsonPackageMetadataGeneralInfo,
    val releases: Map<PyPackageVersion, List<JsonPackageMetadataReleaseInfo>>
)
