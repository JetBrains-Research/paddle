package io.paddle.plugin.python.dependencies.index

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.index.metadata.JsonPackageMetadataInfo
import io.paddle.plugin.python.dependencies.index.utils.*
import kotlinx.serialization.decodeFromString
import org.jsoup.Jsoup


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
        repository: PyPackagesRepository = PyPackagesRepository.PYPI_REPOSITORY
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

    suspend fun downloadMetadata(packageName: String, repository: PyPackagesRepository): JsonPackageMetadataInfo {
        val response: HttpResponse = try {
            httpClient.use {
                it.request("${repository.url}/pypi/$packageName/json")
            }
        } catch (exception: Throwable) {
            error("Failed to download metadata for package '$packageName' from ${repository.name}: ${repository.url}")
        }
        return jsonParser.decodeFromString(response.readText())
    }
}
