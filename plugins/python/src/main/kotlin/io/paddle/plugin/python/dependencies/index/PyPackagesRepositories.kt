package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.PaddlePyConfig
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.extensions.Repositories
import io.paddle.plugin.python.utils.*
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.schedule

class PyPackagesRepositories(
    private val repositories: Set<PyPackagesRepository>,
    val primarySource: PyPackagesRepository,
    useCachedIndex: Boolean = true,
    downloadIndex: Boolean = false
) {
    companion object {
        private const val CACHE_SYNC_PERIOD_MS = 60000L

        fun resolve(data: List<Repositories.Descriptor>): PyPackagesRepositories {
            val repositories = hashSetOf(PyPackagesRepository.PYPI_REPOSITORY)
            var primarySource = PyPackagesRepository.PYPI_REPOSITORY

            for (descriptor in data) {
                val url = descriptor.url.removeSuffix("/").removeSuffix("/simple")
                require(url.isValidUrl()) { "The provided url is invalid: $url" }
                val name = descriptor.name
                val default = descriptor.default ?: false
                val secondary = descriptor.secondary ?: false

                val repo = PyPackagesRepository(url, name)
                if (!secondary) {
                    primarySource = repo
                }
                if (default) {
                    repositories.remove(PyPackagesRepository.PYPI_REPOSITORY)
                    primarySource = repo
                }

                repositories.add(repo)
            }

            return PyPackagesRepositories(repositories, primarySource)
        }

        private fun updateIndex(repositories: Set<PyPackagesRepository>) = runBlocking {
            repositories.map { launch { it.updateIndex() } }.joinAll()
            repositories.forEach { it.saveCache() }
        }
    }

    init {
        if (useCachedIndex) {
            val cachedFiles = PaddlePyConfig.indexDir.toFile().listFiles() ?: emptyArray()
            val newRepositories = HashSet<PyPackagesRepository>()
            for (repo in repositories) {
                cachedFiles.find { it.name == repo.cacheFileName }
                    ?.let { repo.loadCache(it) }
                    ?: newRepositories.add(repo)
            }
            updateIndex(newRepositories)
        }

        if (downloadIndex) {
            updateIndex(repositories)
        }

        Timer("PyPackagesRepositoriesCacheSynchronizer", true)
            .schedule(CACHE_SYNC_PERIOD_MS, CACHE_SYNC_PERIOD_MS) {
                repositories.forEach { it.saveCache() }
            }
    }

    fun findAvailablePackagesByPrefix(prefix: String): Map<PyPackageName, PyPackagesRepository> =
        HashMap<PyPackageName, PyPackagesRepository>().apply {
            for ((repo, names) in repositories.associateWith { it.getPackagesNamesByPrefix(prefix).toList() }) {
                for (pkgName in names) {
                    if (!containsKey(pkgName) || repo == primarySource) {
                        put(pkgName, repo)
                    }
                }
            }
        }

    fun findAvailableDistributionsByPackageName(packageName: String): Map<PyDistributionInfo, PyPackagesRepository> = runBlocking {
        val repoToDistributions = repositories.associateWith { async { it.findAvailableDistributionsByPackageName(packageName) } }
            .run { this.keys.zip(this.values.awaitAll()).toMap() }
        return@runBlocking HashMap<PyDistributionInfo, PyPackagesRepository>().apply {
            for ((repo, distributions) in repoToDistributions) {
                for (distribution in distributions) {
                    if (!containsKey(distribution) || repo == primarySource) {
                        put(distribution, repo)
                    }
                }
            }
        }
    }

    fun getRepositoryByPyPackageUrl(url: PyPackageUrl): PyPackagesRepository {
        return this.repositories.find { repo -> url.startsWith(repo.url) }
            ?: throw IllegalStateException("The repository with specified URL was not found.")
    }

    val all: Set<PyPackagesRepository>
        get() = repositories.toSet()

    val extraSources: Set<PyPackagesRepository>
        get() = repositories.filter { it != primarySource }.toSet()
}
