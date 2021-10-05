package io.paddle.plugin.python.dependencies.index

import com.intellij.util.containers.CollectionFactory
import io.paddle.plugin.python.Config
import io.paddle.utils.StringHashable
import kotlinx.serialization.*
import java.io.File

typealias PyPackageName = String
typealias PyDistributionFilename = String
typealias PyPackageRepositoryUrl = String

@ExperimentalSerializationApi
@Serializable
data class PyPackagesRepository(val url: PyPackageRepositoryUrl) {
    @Transient
    val urlSimple: PyPackageRepositoryUrl = "$url/simple"

    @Transient
    val name: String = StringHashable(url).hash()

    val index: MutableMap<PyPackageName, List<PyDistributionFilename>> = CollectionFactory.createSmallMemoryFootprintMap()

    companion object {
        fun loadFromCache(file: File): PyPackagesRepository {
            return PyPackagesRepositoryIndexer.cborParser.decodeFromByteArray(file.readBytes())
        }
    }

    @Synchronized
    fun updateIndex(name: PyPackageName, distributions: List<PyDistributionFilename>) {
        index[name] = distributions
    }

    fun save() {
        Config.indexDir.resolve("$name.json").toFile()
            .writeBytes(PyPackagesRepositoryIndexer.cborParser.encodeToByteArray(this))
    }
}
