package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.Config
import io.paddle.utils.StringHashable
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
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

    fun save() = Config.indexDir.resolve("$name.json").toFile().writeText(Json.encodeToString(this))
}
