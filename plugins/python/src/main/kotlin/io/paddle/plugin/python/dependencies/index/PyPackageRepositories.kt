package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.Config
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import kotlinx.coroutines.*
import kotlinx.serialization.ExperimentalSerializationApi
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.schedule

@ExperimentalSerializationApi
object PyPackageRepositories {
    const val CACHE_SYNC_PERIOD_MS = 60000L
    private val repositories: MutableSet<PyPackagesRepository> = HashSet()
    private val lock = ReentrantReadWriteLock()

    init {
        Config.indexDir.toFile().listFiles()
            ?.map { file -> PyPackagesRepository.loadFromFile(file) }
            ?.let { repositories.addAll(it) }

        if (repositories.isEmpty()) {
            repositories.add(PYPI_REPOSITORY)
            updateAllCachedPackageNames()
        }

        Timer("PyPackageRepositoriesCacheSynchronizer", true).schedule(CACHE_SYNC_PERIOD_MS, CACHE_SYNC_PERIOD_MS) {
            try {
                lock.readLock().lock()
                repositories.forEach { it.save() }
            } finally {
                lock.readLock().unlock()
            }
        }
    }

    fun findAvailablePackagesByPrefix(prefix: String): Map<PyPackagesRepository, List<PyPackageName>> {
        return repositories.associateWith { repo -> repo.packageNamesCache.filter { it.startsWith(prefix) } }
    }

    fun findAvailableDistributionsByPackage(packageName: String): Map<PyPackagesRepository, List<PyDistributionInfo>> {
        return repositories.associateWith { repo -> repo[packageName] }
    }

    fun updateAllCachedPackageNames() = try {
        lock.writeLock().lock()
        runBlocking {
            repositories.map { launch { it.updatePackageNamesCache() } }
                .also { jobs -> jobs.joinAll() }
            repositories.forEach { repo -> repo.save() }
        }
    } finally {
        lock.writeLock().unlock()
    }

    fun addRepository(url: String) = try {
        lock.writeLock().lock()
        repositories.add(PyPackagesRepository(url))
    } finally {
        lock.writeLock().unlock()
    }

    fun removeRepository(url: String) = try {
        lock.writeLock().lock()
        repositories.removeIf { it.url == url }
    } finally {
        lock.writeLock().unlock()
    }

    fun getRepositoryByUrl(url: String): PyPackagesRepository? = repositories.find { it.url == url }

    fun getAvailableRepositories(): Set<PyPackagesRepository> = repositories.toSet()
}
