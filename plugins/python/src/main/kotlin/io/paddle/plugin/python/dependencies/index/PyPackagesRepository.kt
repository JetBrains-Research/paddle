package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.dependencies.PythonDependenciesConfig
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.index.utils.*
import io.paddle.plugin.python.dependencies.index.wordlist.PackedWordList
import io.paddle.plugin.python.dependencies.index.wordlist.PackedWordListSerializer
import io.paddle.utils.StringHashable
import kotlinx.serialization.*
import java.io.File

@Serializable
data class PyPackagesRepository(val url: PyPackagesRepositoryUrl, val name: String) {
    @Serializable(with = PackedWordListSerializer::class)
    private var packagesNamesCache: PackedWordList = PackedWordList.empty

    private val distributionsCache: MutableMap<PyPackageName, List<PyDistributionInfo>> = HashMap()

    @Transient
    val urlSimple: PyPackagesRepositoryUrl = url.join("simple")

    @Transient
    val cacheFileName: String = StringHashable(url).hash()

    companion object {
        val PYPI_REPOSITORY = PyPackagesRepository("https://pypi.org", "pypi")
    }

    suspend fun updateIndex() {
        this.packagesNamesCache = PackedWordList(PyPackagesRepositoryIndexer.downloadPackagesNames(this).toSet())
    }

    fun loadCache(file: File) {
        require(file.name == this.cacheFileName)
        val cachedCopy: PyPackagesRepository = jsonParser.decodeFromString(file.readText())
        this.packagesNamesCache = cachedCopy.packagesNamesCache
    }

    fun getPackagesNamesByPrefix(prefix: String): Sequence<PyPackageName> = packagesNamesCache.prefix(prefix)

    suspend fun getDistributions(packageName: PyPackageName): List<PyDistributionInfo> {
        return distributionsCache.getOrPut(packageName) {
            PyPackagesRepositoryIndexer.downloadDistributionsList(packageName, this@PyPackagesRepository)
        }
    }

    fun saveCache() {
        PythonDependenciesConfig.indexDir.resolve(this.cacheFileName).toFile()
            .writeText(jsonParser.encodeToString(this))
    }
}
