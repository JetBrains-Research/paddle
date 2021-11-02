package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.dependencies.PythonDependenciesConfig
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.index.utils.*
import io.paddle.plugin.python.dependencies.index.wordlist.PackedWordList
import io.paddle.plugin.python.dependencies.index.wordlist.PackedWordListSerializer
import io.paddle.plugin.python.extensions.Requirements
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

    suspend fun getDistributions(packageName: PyPackageName, useCache: Boolean = true): List<PyDistributionInfo> {
        val distributions = PyPackagesRepositoryIndexer.downloadDistributionsList(packageName, this@PyPackagesRepository)
        return if (useCache) {
            distributionsCache.getOrPut(packageName) { distributions }
        } else {
            distributions
        }
    }

    fun saveCache() {
        PythonDependenciesConfig.indexDir.resolve(this.cacheFileName).toFile()
            .writeText(jsonParser.encodeToString(this))
    }

    /**
     * Searches for the particular package (name, version) in the current repository.
     * If nothing have been found, returns null.
     *
     * TODO: take current platform and python-interpreter tags for consideration as well
     *
     * TODO: use this method to resolve packages during installation by Paddle, not by pip
     */
    suspend fun search(descriptor: Requirements.Descriptor): PyDistributionInfo? {
        val distributions = getDistributions(descriptor.name)
        return try {
            distributions.find { it.version == descriptor.version }
        } catch (exception: Throwable) {
            null
        }
    }
}
