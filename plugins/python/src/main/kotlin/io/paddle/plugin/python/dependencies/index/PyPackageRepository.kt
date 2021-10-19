package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.Config
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.index.wordlist.PackedWordList
import io.paddle.plugin.python.dependencies.index.wordlist.WordList
import io.paddle.plugin.python.dependencies.isValidUrl
import io.paddle.utils.StringHashable
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.*
import java.io.File

typealias PyPackageName = String
typealias PyPackageRepositoryUrl = String

const val PYPI_URL = "https://pypi.org"

val PYPI_REPOSITORY = PyPackagesRepository(PYPI_URL)

@Serializable
data class PyPackagesRepository(val url: PyPackageRepositoryUrl) {
    @Transient
    private lateinit var packagesNamesCache: WordList

    private val distributionsCache: MutableMap<PyPackageName, List<PyDistributionInfo>> = HashMap()

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

    companion object {
        fun loadFromFile(file: File): PyPackagesRepository {
            return jsonParser.decodeFromString(file.readText())
        }
    }

    suspend fun updateCache() {
        this.packagesNamesCache = PackedWordList(
            words = PyPackagesRepositoryIndexer.downloadPackagesNames(this).toSet()
        )
    }

    fun getPackagesNamesByPrefix(prefix: String): Sequence<PyPackageName> = packagesNamesCache.prefix(prefix)

    fun getDistributions(packageName: PyPackageName): List<PyDistributionInfo> {
        return distributionsCache.getOrPut(packageName) {
            runBlocking {
                PyPackagesRepositoryIndexer.downloadDistributionsList(packageName, this@PyPackagesRepository)
            }
        }
    }

    fun save() {
        Config.indexDir.resolve("$hashcode.json").toFile()
            .writeText(jsonParser.encodeToString(this))
    }
}
