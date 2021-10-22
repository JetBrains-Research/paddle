package io.paddle.plugin.python.dependencies.index

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.index.metadata.JsonPackageMetadataInfo
import kotlinx.serialization.decodeFromString
import org.jsoup.Jsoup


object PyPackagesRepositoryIndexer {
    suspend fun downloadPackagesNames(repository: PyPackagesRepository): Collection<PyPackageName> {
        return httpClient.request<HttpStatement>(repository.urlSimple).execute { response ->
            val allNamesDocument = Jsoup.parse(response.readText())
            return@execute allNamesDocument.body().getElementsByTag("a").map { it.text() }
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
            emptyList()
        }
    }

    suspend fun downloadMetadata(packageName: String, repository: PyPackagesRepository): JsonPackageMetadataInfo {
        val response: HttpResponse = httpClient.use {
            it.request("${repository.url}/pypi/$packageName/json")
        }
        return jsonParser.decodeFromString(response.readText())
    }
}
