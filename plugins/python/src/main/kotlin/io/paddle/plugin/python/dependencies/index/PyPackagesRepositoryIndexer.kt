package io.paddle.plugin.python.dependencies.index

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.index.metadata.JsonPackageMetadataInfo
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import org.jsoup.Jsoup


@ExperimentalSerializationApi
object PyPackagesRepositoryIndexer {
    suspend fun downloadPackageNames(repository: PyPackagesRepository): Collection<PyPackageName> {
        val allNamesHtml = httpClient.request<HttpResponse>(repository.urlSimple).readText()
        val allNamesDocument = Jsoup.parse(allNamesHtml)
        return allNamesDocument.body().getElementsByTag("a").map { it.text() }
    }

    suspend fun downloadDistributionsList(packageName: String, repository: PyPackagesRepository = PYPI_REPOSITORY): List<PyDistributionInfo> {
        val response: HttpResponse = httpClient.use {
            it.request("${repository.urlSimple}/$packageName")
        }
        val distributionsPage = Jsoup.parse(response.readText())
        return distributionsPage.body().getElementsByTag("a")
            .mapNotNull { PyDistributionInfo.fromString(it.text()) }
    }

    suspend fun downloadMetadata(packageName: String, repository: PyPackagesRepository): JsonPackageMetadataInfo {
        val response: HttpResponse = httpClient.use {
            it.request("${repository.url}/pypi/$packageName/json")
        }
        return cborParser.decodeFromByteArray(response.readBytes())
    }
}
