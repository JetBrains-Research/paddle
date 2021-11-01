package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.dependencies.PythonDependenciesConfig
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.index.utils.PyPackageName
import io.paddle.plugin.python.dependencies.isValidUrl
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.HashMap
import kotlin.concurrent.schedule

class PyPackagesRepositories(
    private val repositories: Set<PyPackagesRepository>,
    val primarySource: PyPackagesRepository,
    useCachedIndex: Boolean = true,
    downloadIndex: Boolean = false
) {
    companion object {
        private const val CACHE_SYNC_PERIOD_MS = 60000L

        fun parse(data: List<Map<String, String>>): PyPackagesRepositories {
            val repositories = hashSetOf(PyPackagesRepository.PYPI_REPOSITORY)
            var primarySource = PyPackagesRepository.PYPI_REPOSITORY

            for (repoConfig in data) {
                val url = repoConfig["url"]?.removeSuffix("/")?.removeSuffix("/simple")
                    ?: error("For some repository URL is not specified: $repoConfig}")
                require(url.isValidUrl()) { "The provided url is invalid: $url" }
                val name = repoConfig["name"]
                    ?: error("For some repository NAME is not specified: $repoConfig")
                val default = repoConfig["default"]?.toBoolean() ?: false
                val secondary = repoConfig["secondary"]?.toBoolean() ?: false

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
            val jobs = repositories.map { launch { it.updateIndex() } }
            jobs.joinAll()
            repositories.forEach { it.save() }
        }
    }

    init {
        if (useCachedIndex) {
            val cachedFiles = PythonDependenciesConfig.indexDir.toFile().listFiles() ?: emptyArray()
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
                repositories.forEach { it.save() }
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

    fun findAvailableDistributionsByPackage(packageName: String): Map<PyDistributionInfo, PyPackagesRepository> = runBlocking {
        val repoToDistributions = repositories.associateWith { async { it.getDistributions(packageName) } }
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

    val all: Set<PyPackagesRepository>
        get() = repositories.toSet()
}
