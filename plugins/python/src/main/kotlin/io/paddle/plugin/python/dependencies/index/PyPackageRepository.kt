package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.Config
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.isValidUrl
import io.paddle.utils.StringHashable
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import java.io.File

typealias PyPackageName = String
typealias PyPackageRepositoryUrl = String

const val PYPI_URL = "https://pypi.org"

@ExperimentalSerializationApi
val PYPI_REPOSITORY = PyPackagesRepository(PYPI_URL)

@ExperimentalSerializationApi
@Serializable
data class PyPackagesRepository(val url: PyPackageRepositoryUrl) {
    @Transient
    val urlSimple: PyPackageRepositoryUrl = "$url/simple"

    @Transient
    val hashcode: String = StringHashable(url).hash()

    init {
        require(url.isValidUrl()) { "The specified URL=$url is not valid." }
        if (url.endsWith('/')) {
            url.dropLast(1)
        }
    }

    val packageNamesCache: MutableSet<PyPackageName> = HashSet()
    private val distributionsCache: MutableMap<PyPackageName, List<PyDistributionInfo>> = HashMap()

    companion object {
        fun loadFromFile(file: File): PyPackagesRepository {
            return cborParser.decodeFromByteArray(file.readBytes())
        }
    }

    suspend fun updatePackageNamesCache() {
        packageNamesCache.clear()
        packageNamesCache.addAll(PyPackagesRepositoryIndexer.downloadPackageNames(this))
    }

    operator fun get(packageName: PyPackageName): List<PyDistributionInfo> {
        return distributionsCache.getOrPut(packageName) {
            runBlocking {
                PyPackagesRepositoryIndexer.downloadDistributionsList(packageName, this@PyPackagesRepository)
            }
        }
    }

    fun save() {
        Config.indexDir.resolve("$hashcode.json").toFile()
            .writeBytes(cborParser.encodeToByteArray(this))
    }
}
