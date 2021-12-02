package io.paddle.plugin.python.dependencies.index

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.index.metadata.JsonPackageMetadataInfo
import io.paddle.plugin.python.dependencies.index.utils.PyPackageName
import io.paddle.plugin.python.dependencies.index.utils.httpClient
import io.paddle.plugin.python.dependencies.index.utils.join
import io.paddle.plugin.python.dependencies.index.utils.jsonParser
import kotlinx.serialization.decodeFromString
import org.jsoup.Jsoup
import java.io.File
import java.nio.file.Path


object PyPackagesRepositoryIndexer {
    suspend fun downloadPackagesNames(repository: PyPackagesRepository): Collection<PyPackageName> {
        return try {
            httpClient.request<HttpStatement>(repository.urlSimple).execute { response ->
                val allNamesDocument = Jsoup.parse(response.readText())
                return@execute allNamesDocument.body().getElementsByTag("a").map { it.text() }
            }
        } catch (exception: Throwable) {
            error("Failed to update index of available packages for PyPI repository ${repository.name}: ${repository.url}")
        }
    }

    suspend fun downloadDistributionsList(
        packageName: String,
        repository: PyPackagesRepository
    ): List<PyDistributionInfo> {
        return try {
            httpClient.request<HttpStatement>(repository.urlSimple.join(packageName)).execute { response ->
                val distributionsPage = Jsoup.parse(response.readText())
                return@execute distributionsPage.body().getElementsByTag("a")
                    .mapNotNull { PyDistributionInfo.fromString(it.text()) }
            }
        } catch (exception: Throwable) {
            error("Failed to update index of available distributions for PyPI repository ${repository.name}: ${repository.url}")
        }
    }

    suspend fun downloadDistribution(
        distributionInfo: PyDistributionInfo,
        repository: PyPackagesRepository,
        path: Path
    ): File {
        TODO()
    }

    suspend fun downloadMetadata(packageName: String, repository: PyPackagesRepository): JsonPackageMetadataInfo {
        val response: HttpResponse = try {
            httpClient.use {
                it.request(repository.url.join("pypi", packageName, "json"))
            }
        } catch (exception: Throwable) {
            error("Failed to download metadata for package '$packageName' from ${repository.name}: ${repository.url}")
        }
        return jsonParser.decodeFromString(response.readText())
    }
}
