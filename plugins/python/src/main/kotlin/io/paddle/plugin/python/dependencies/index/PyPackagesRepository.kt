package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.dependencies.PythonDependenciesConfig
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.index.utils.PyPackageName
import io.paddle.plugin.python.dependencies.index.utils.PyPackagesRepositoryUrl
import io.paddle.plugin.python.dependencies.index.utils.join
import io.paddle.plugin.python.dependencies.index.utils.jsonParser
import io.paddle.plugin.python.dependencies.index.wordlist.PackedWordList
import io.paddle.plugin.python.dependencies.index.wordlist.PackedWordListSerializer
import io.paddle.plugin.python.extensions.Requirements
import io.paddle.utils.StringHashable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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

        fun loadMetadata(file: File): PyPackagesRepository {
            val (url, name) = file.readLines()
            return PyPackagesRepository(url, name)
        }
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

    suspend fun findAvailableDistributionsByPackageName(packageName: PyPackageName, useCache: Boolean = true): List<PyDistributionInfo> {
        val distributions = PyPackagesRepositoryIndexer.downloadDistributionsList(packageName, this)
        return if (useCache) {
            distributionsCache.getOrPut(packageName) { distributions }
        } else {
            distributions
        }
    }

    suspend fun findAvailableDistributions(descriptor: Requirements.Descriptor): List<PyDistributionInfo> {
        return try {
            val distributions = findAvailableDistributionsByPackageName(descriptor.name, useCache = false)
            distributions.filter { it.version == descriptor.version }
        } catch (exception: Throwable) {
            emptyList()
        }
    }

    fun saveCache() {
        PythonDependenciesConfig.indexDir.resolve(this.cacheFileName).toFile()
            .writeText(jsonParser.encodeToString(this))
    }
}
