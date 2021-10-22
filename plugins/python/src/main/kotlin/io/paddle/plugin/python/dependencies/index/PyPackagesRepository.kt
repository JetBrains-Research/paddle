package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.Config
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
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

        fun loadFromFile(file: File): PyPackagesRepository {
            return jsonParser.decodeFromString(file.readText())
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

    suspend fun getDistributions(packageName: PyPackageName): List<PyDistributionInfo> {
        return distributionsCache.getOrPut(packageName) {
            PyPackagesRepositoryIndexer.downloadDistributionsList(packageName, this@PyPackagesRepository)
        }
    }

    fun save() {
        Config.indexDir.resolve(this.cacheFileName).toFile()
            .writeText(jsonParser.encodeToString(this))
    }

    override fun hashCode(): Int {
        return url.hashCode() * 37 + name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PyPackagesRepository

        if (url != other.url) return false
        if (name != other.name) return false

        return true
    }
}
