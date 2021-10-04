package io.paddle.plugin.python.dependencies.index

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.paddle.plugin.python.Config
import io.paddle.plugin.python.dependencies.index.metadata.JsonPackageMetadataInfo
import io.paddle.plugin.python.dependencies.isValidUrl
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import sun.misc.Signal
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.set
import kotlin.system.exitProcess
import kotlin.system.measureTimeMillis

const val PYPI_URL = "https://pypi.org"

object PyPackagesRepositoryIndexer {
    private const val THREADS_COUNT = 24
    private const val TIMEOUT_MS = 10000L

    private val log = LoggerFactory.getLogger(javaClass)

    private val repositories: MutableSet<PyPackagesRepository> = hashSetOf()
    private val packagesNamesCache: MutableMap<PyPackagesRepository, MutableList<PyPackageName>> = ConcurrentHashMap()

    internal val jsonParser = Json {
        ignoreUnknownKeys = true
    }

    private val httpClient = HttpClient(CIO) {
        engine {
            threadsCount = THREADS_COUNT
            maxConnectionsCount = 1000
            endpoint {
                connectAttempts = 5
                connectTimeout = TIMEOUT_MS
                requestTimeout = TIMEOUT_MS
                socketTimeout = TIMEOUT_MS
            }
        }
    }

    init {
        Config.indexDir.toFile().listFiles()?.forEach { file ->
            val repo = PyPackagesRepository.loadFromCache(file)
            packagesNamesCache[repo] = repo.index.keys.toMutableList()
            repositories.add(repo)
        }
        if (getRepositoryByUrl(PYPI_URL) == null) {
            repositories.add(PyPackagesRepository(PYPI_URL))
        }
    }

    fun findAvailablePackagesByPrefix(prefix: String): Map<PyPackagesRepository, List<PyPackageName>> {
        return packagesNamesCache.mapValues { it.value.filter { name -> name.startsWith(prefix) } }
    }

    fun findAvailableDistributionsByPackage(packageName: String): Map<PyPackageRepositoryUrl, List<PyDistributionFilename>> {
        return repositories.associateBy(PyPackagesRepository::url) { repo -> repo.index[packageName] ?: emptyList() }
    }

    fun updateAllIndices() = repositories.forEach { updateIndex(it) }

    fun updateIndex(repository: PyPackagesRepository) = runBlocking {
        val allNamesHtml = httpClient.request<HttpResponse>(repository.urlSimple).readText()
        val allNamesDocument = Jsoup.parse(allNamesHtml)

        val progressCounter = AtomicInteger(0)
        val requestSemaphore = Semaphore(THREADS_COUNT)
        Signal.handle(Signal("INT")) {
            repository.save()
            exitProcess(0)
        }

        coroutineScope {
            allNamesDocument.body().getElementsByTag("a").map { link ->
                launch {
                    val packageName = link.text()
                    val href = link.attr("href")
                    packagesNamesCache.putIfAbsent(repository, arrayListOf())?.add(packageName)
                    try {
                        val response = requestSemaphore.withPermit {
                            httpClient.request<HttpResponse>(repository.url + href)
                        }
                        val distributionsPage = Jsoup.parse(response.readText())
                        val distributions = distributionsPage.body().getElementsByTag("a").map { it.text() }
                        repository.index[packageName] = distributions
                        log.info("Done with package #${progressCounter.incrementAndGet()}: $packageName")
                    } catch (cause: Throwable) {
                        log.warn("Failed to process package: #${progressCounter.incrementAndGet()}: $packageName")
                        log.warn(cause.stackTraceToString())
                        return@launch
                    }
                }
            }
        }

        repository.save()
    }

    fun downloadMetadata(packageName: String, repositoryUrl: String = PYPI_URL): JsonPackageMetadataInfo = runBlocking {
        val response: String = withContext(Dispatchers.Default) {
            httpClient.use { it.request<HttpResponse>("$repositoryUrl/pypi/$packageName/json").readText() }
        }
        return@runBlocking jsonParser.decodeFromString(response)
    }

    fun addRepository(url: String) {
        if (!url.isValidUrl()) {
            error("The specified URL=$url is not valid.")
        }
        if (url.endsWith('/')) {
            url.dropLast(1)
        }
        PyPackagesRepository(url).also {
            repositories.add(it)
            updateIndex(it)
        }
    }

    fun removeRepository(url: String) = repositories.removeIf { it.url == url }

    fun getRepositoryByUrl(url: String): PyPackagesRepository? = repositories.find { it.url == url }

    fun getAvailableRepositories(): Set<PyPackagesRepository> = repositories.toSet()
}


fun main() {
    val timeInMillis = measureTimeMillis {
        PyPackagesRepositoryIndexer.updateAllIndices()
    }
    println("(The operation took $timeInMillis ms)")
}
