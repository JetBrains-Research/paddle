package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.Config
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.index.utils.PyPackageName
import kotlinx.coroutines.*
import java.util.*
import kotlin.concurrent.schedule

class PyPackagesRepositories(
    private val repositories: MutableSet<PyPackagesRepository>,
    useCached: Boolean = true,
    updateAllIndex: Boolean = false,
    default: PyPackagesRepository = PyPackagesRepository.PYPI_REPOSITORY
) {
    companion object {
        private const val CACHE_SYNC_PERIOD_MS = 60000L

        private fun updateIndex(repositories: Set<PyPackagesRepository>) = runBlocking {
            val jobs = repositories.map { launch { it.updateIndex() } }
            jobs.joinAll()
            repositories.forEach { it.save() }
        }
    }

    init {
        if (useCached) {
            val cachedFiles = Config.indexDir.toFile().listFiles() ?: emptyArray()
            val repositoriesToUpdate = HashSet<PyPackagesRepository>()
            for (repo in repositories) {
                cachedFiles.find { it.name == repo.cacheFileName }?.let { repo.loadCache(it) }
                    ?: repositoriesToUpdate.add(repo)
            }
            updateIndex(repositoriesToUpdate)
        }

        if (updateAllIndex) {
            updateIndex(repositories)
        }

        Timer("PyPackagesRepositoriesCacheSynchronizer", true)
            .schedule(CACHE_SYNC_PERIOD_MS, CACHE_SYNC_PERIOD_MS) {
                repositories.forEach { it.save() }
            }
    }

    fun findAvailablePackagesByPrefix(prefix: String): Map<PyPackagesRepository, List<PyPackageName>> {
        return repositories.associateWith { it.getPackagesNamesByPrefix(prefix).toList() }
    }

    fun findAvailableDistributionsByPackage(packageName: String): Map<PyPackagesRepository, List<PyDistributionInfo>> = runBlocking {
        repositories.associateWith { async { it.getDistributions(packageName) } }
            .run { this.keys.zip(this.values.awaitAll()).toMap() }
    }

    fun addRepository(url: String, name: String) = repositories.add(PyPackagesRepository(url, name))

    fun removeRepository(url: String) = repositories.removeIf { it.url == url }

    fun getRepositoryByUrl(url: String): PyPackagesRepository? = repositories.find { it.url == url }

    val all: Set<PyPackagesRepository>
        get() = repositories.toSet()
}
