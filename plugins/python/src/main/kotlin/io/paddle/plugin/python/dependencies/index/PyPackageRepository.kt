package io.paddle.plugin.python.dependencies.index

import io.paddle.utils.StringHashable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.concurrent.ConcurrentHashMap

typealias PyPackageName = String
typealias PyDistributionFilename = String
typealias PyPackageRepositoryUrl = String

@Serializable
data class PyPackagesRepository(val url: PyPackageRepositoryUrl) {
    @Transient
    val urlSimple: PyPackageRepositoryUrl = "$url/simple"

    @Transient
    val name: String = StringHashable(url).hash()

    val index: MutableMap<PyPackageName, List<PyDistributionFilename>> = ConcurrentHashMap()
}
