package io.paddle.plugin.python.dependencies.index.metadata

import io.paddle.plugin.python.dependencies.packages.PyPackageVersion
import kotlinx.serialization.Serializable

@Serializable
data class JsonPackageMetadataInfo(
    val info: JsonPackageMetadataGeneralInfo,
    // FIXME: this field will be deprecated soon: https://warehouse.pypa.io/api-reference/json.html
    val releases: Map<PyPackageVersion, List<JsonPackageMetadataReleaseInfo>>
)
