package io.paddle.plugin.python.dependencies.repositories

import io.paddle.plugin.python.PaddlePyConfig
import io.paddle.plugin.python.dependencies.index.PyPackageRepositoryIndexer
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.index.wordlist.PackedWordList
import io.paddle.plugin.python.dependencies.index.wordlist.PackedWordListSerializer
import io.paddle.plugin.python.utils.*
import io.paddle.utils.StringHashable
import kotlinx.serialization.*
import java.io.File

@Serializable
class PyPackageRepository(val url: PyPackagesRepositoryUrl, val name: String) {
    constructor(metadata: Metadata) : this(metadata.url, metadata.name)

    @Serializable
    data class Metadata(val url: PyPackagesRepositoryUrl, val name: String)

    val metadata = Metadata(url, name)

    companion object {
        val PYPI_REPOSITORY = PyPackageRepository("https://pypi.org", "pypi")
    }

    // Index is loaded from cache
    @Serializable(with = PackedWordListSerializer::class)
    private var packagesNamesCache: PackedWordList = PackedWordList.empty

    // Index is loaded from cache
    private val distributionsCache: MutableMap<PyPackageName, List<PyDistributionInfo>> = HashMap()

    @Transient
    val urlSimple: PyPackagesRepositoryUrl = url.join("simple")

    @Transient
    val cacheFileName: String = StringHashable(url).hash()

    suspend fun updateIndex() {
        packagesNamesCache = PackedWordList(PyPackageRepositoryIndexer.downloadPackagesNames(this).toSet())
    }

    fun loadCache(file: File) {
        require(file.name == this.cacheFileName)
        val cachedCopy: PyPackageRepository = jsonParser.decodeFromString(file.readText())
        packagesNamesCache = cachedCopy.packagesNamesCache
    }

    fun getPackagesNamesByPrefix(prefix: String): Sequence<PyPackageName> = packagesNamesCache.prefix(prefix)

    suspend fun findAvailableDistributionsByPackageName(packageName: PyPackageName, useCache: Boolean = true): List<PyDistributionInfo> {
        val distributions = PyPackageRepositoryIndexer.downloadDistributionsList(packageName, this)
        return if (useCache) {
            distributionsCache.getOrPut(packageName) { distributions }
        } else {
            distributions
        }
    }

    fun saveCache() {
        PaddlePyConfig.indexDir.resolve(this.cacheFileName).toFile()
            .writeText(jsonParser.encodeToString(this))
    }

    override fun hashCode() = metadata.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PyPackageRepository

        return metadata == other.metadata
    }
}
