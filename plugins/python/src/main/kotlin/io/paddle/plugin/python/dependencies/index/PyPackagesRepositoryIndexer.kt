package io.paddle.plugin.python.dependencies.index

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.HttpTimeout.Feature.INFINITE_TIMEOUT_MS
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.paddle.plugin.python.Config
import io.paddle.plugin.python.dependencies.index.metadata.JsonPackageMetadataInfo
import io.paddle.plugin.python.dependencies.isValidUrl
import io.paddle.plugin.python.dependencies.parallelForEach
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

const val PYPI_URL = "https://pypi.org"


object PyPackagesRepositoryIndexer {
    private val log = LoggerFactory.getLogger(javaClass)

    private val repositories: MutableSet<PyPackagesRepository> = hashSetOf(PyPackagesRepository(PYPI_URL))
    private val packagesNamesCache: MutableSet<String> = hashSetOf()

    private val jsonParser = Json { ignoreUnknownKeys = true }
    private val httpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = INFINITE_TIMEOUT_MS
            socketTimeoutMillis = INFINITE_TIMEOUT_MS
            connectTimeoutMillis = INFINITE_TIMEOUT_MS
        }
    }

    fun updateAllIndices() {
        for (repository in repositories) {
            updateIndex(repository.url)
        }
    }

    fun updateIndex(repositoryUrl: String) = runBlocking {
        val repository = getRepositoryByUrl(repositoryUrl) ?: return@runBlocking
        val allNamesDocument = Jsoup.connect(repository.urlSimple).get()
        val progressCounter = AtomicInteger(0)
        allNamesDocument.body().getElementsByTag("a").subList(0, 1000).parallelForEach { link ->
            val packageName = link.text()
            val href = link.attr("href")
            packagesNamesCache.add(packageName)
            try {
                val response = httpClient.request<HttpResponse>(repository.url + href)
                val downloadsPage = Jsoup.parse(response.readText())
                val distributions = downloadsPage.body().getElementsByTag("a").map { it.text() }
                repository.index[packageName] = distributions
                log.info("Done with package #${progressCounter.incrementAndGet()}: $packageName")
            } catch (cause: Throwable) {
                log.warn("Failed to process package: #${progressCounter.incrementAndGet()}: $packageName")
                log.warn(cause.stackTraceToString())
                return@parallelForEach
            }
        }
        val repoIndexFile = Config.indexDir.resolve("${repository.name}.json").toFile()
        repoIndexFile.writeText(jsonParser.encodeToString(repository))
    }

    fun downloadMetadata(packageName: String, repositoryUrl: String = PYPI_URL): JsonPackageMetadataInfo = runBlocking {
        val response: String = withContext(Dispatchers.Default) {
            httpClient.request<HttpResponse>("$repositoryUrl/pypi/$packageName/json").readText()
        }
        return@runBlocking jsonParser.decodeFromString(response)
    }

    fun addRepository(url: String) {
        if (!url.isValidUrl()) {
            error("The specified repository URL=$url is not a valid URL.")
        }
        if (url.endsWith('/')) {
            url.dropLast(1)
        }
        repositories.add(PyPackagesRepository(url))
    }

    fun removeRepository(url: String) = repositories.removeIf { it.url == url }

    fun getRepositoryByUrl(url: String) = repositories.find { it.url == url }

    fun getAvailableRepositories(): Set<PyPackagesRepository> = repositories.toSet()
}


fun main() {
    val timeInMillis = measureTimeMillis {
        PyPackagesRepositoryIndexer.updateAllIndices()
    }
    println("(The operation took $timeInMillis ms)")
}
