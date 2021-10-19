package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.Config
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.schedule

object PyPackageRepositories {
    private const val CACHE_SYNC_PERIOD_MS = 60000L
    private val repositories: MutableSet<PyPackagesRepository> = ConcurrentHashMap.newKeySet()

    init {
        Config.indexDir.toFile().listFiles()
            ?.map { file -> PyPackagesRepository.loadFromFile(file) }
            ?.let { repositories.addAll(it) }

        if (repositories.isEmpty()) {
            repositories.add(PYPI_REPOSITORY)
            updateCache()
        }

        Timer("PyPackageRepositoriesCacheSynchronizer", true).schedule(CACHE_SYNC_PERIOD_MS, CACHE_SYNC_PERIOD_MS) {
            repositories.forEach { it.save() }
        }
    }

    fun findAvailablePackagesByPrefix(prefix: String): Map<PyPackagesRepository, List<PyPackageName>> {
        return repositories.associateWith { it.getPackagesNamesByPrefix(prefix).toList() }
    }

    fun findAvailableDistributionsByPackage(packageName: String): Map<PyPackagesRepository, List<PyDistributionInfo>> {
        return repositories.associateWith { it.getDistributions(packageName) }
    }

    private fun updateCache() = runBlocking {
        val jobs = repositories.map { launch { it.updateCache() } }
        jobs.joinAll()
        repositories.forEach { it.save() }
    }

    fun addRepository(url: String) = repositories.add(PyPackagesRepository(url))

    fun removeRepository(url: String) = repositories.removeIf { it.url == url }

    fun getRepositoryByUrl(url: String): PyPackagesRepository? = repositories.find { it.url == url }

    fun getAvailableRepositories(): Set<PyPackagesRepository> = repositories.toSet()
}
