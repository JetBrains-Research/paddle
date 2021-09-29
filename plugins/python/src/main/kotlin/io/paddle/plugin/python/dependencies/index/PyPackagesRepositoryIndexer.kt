package io.paddle.plugin.python.dependencies.index

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.paddle.plugin.python.dependencies.isValidUrl
import io.paddle.plugin.python.dependencies.parallelForEach
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.measureTimeMillis

const val PYPI_URL = "https://pypi.org"

val PADDLE_HOME: Path = Paths.get(System.getProperty("user.home"), ".paddle")
val INDEX_DIR: Path = PADDLE_HOME.resolve(".index")
    .also { it.toFile().mkdirs() }

val TIMEOUT_MILLIS = Duration.ofHours(1).toMillis()

data class PyPackagesRepository(val url: String, val urlSimple: String = "$url/simple") {
    companion object {
        fun getPyPi() = PyPackagesRepository(PYPI_URL)
    }
}

object PyPackagesRepositoryIndexer {
    private val repositories: MutableSet<PyPackagesRepository> = hashSetOf(PyPackagesRepository.getPyPi())
    private val nameIndex: MutableCollection<String> = hashSetOf()
    private val packageInfoIndex: MutableMap<String, String> = ConcurrentHashMap()

    private val jsonParser = Json { ignoreUnknownKeys = true }

    private val httpClient = HttpClient(CIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = TIMEOUT_MILLIS
            socketTimeoutMillis = TIMEOUT_MILLIS
            connectTimeoutMillis = TIMEOUT_MILLIS
        }
    }

    fun updateIndex() = runBlocking(Dispatchers.Default) {
        for (repository in repositories) {
            val document = Jsoup.connect(repository.urlSimple).get()
            for (link in document.body().getElementsByTag("a")) {
                val packageName = link.text()
                nameIndex.add(packageName)
            }
            File(PADDLE_HOME.resolve("pypi.txt").toUri()).writeText(
                text = nameIndex.joinToString("\n")
            )
            nameIndex.parallelForEach { packageName ->
                try {
                    val response = httpClient.request<HttpResponse>(repository.url + "/pypi/$packageName/json")
                    val json = response.readText()
                    packageInfoIndex[packageName] = json

                    val file = File(INDEX_DIR.resolve("$packageName.json").toUri())
                    if (!file.exists()) {
                        file.writeText(json)
                    }
                } catch (cause: ClientRequestException) {
                    return@parallelForEach
                }
            }
        }
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

    fun removeRepository(repository: PyPackagesRepository) = repositories.remove(repository)

    fun getAvailableRepositories(): Set<PyPackagesRepository> = repositories.toSet()
}


fun main() {
    val timeInMillis = measureTimeMillis {
        PyPackagesRepositoryIndexer.updateIndex()
    }
    println("(The operation took $timeInMillis ms)")
}
