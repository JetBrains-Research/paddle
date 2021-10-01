package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.Config
import io.paddle.utils.StringHashable
import kotlinx.serialization.*
import java.io.File
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

    companion object {
        fun loadFromCache(file: File): PyPackagesRepository {
            return PyPackagesRepositoryIndexer.jsonParser.decodeFromString(file.readText())
        }
    }

    fun save() {
        Config.indexDir.resolve("$name.json").toFile()
            .writeText(PyPackagesRepositoryIndexer.jsonParser.encodeToString(this))
    }
}
