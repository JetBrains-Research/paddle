package io.paddle.plugin.python.dependencies.index

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.index.metadata.JsonPackageMetadataInfo
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.utils.*
import io.paddle.terminal.Terminal
import kotlinx.serialization.decodeFromString
import org.jsoup.Jsoup
import java.io.File


object PyPackageRepositoryIndexer {
    suspend fun downloadPackagesNames(repository: PyPackageRepository): Collection<PyPackageName> {
        return try {
            httpClient.request<HttpStatement>(repository.basicAuthUrlSimple).execute { response ->
                val allNamesDocument = Jsoup.parse(response.readText())
                return@execute allNamesDocument.body().getElementsByTag("a").map { it.text() }
            }
        } catch (exception: Throwable) {
            error("Failed to update index of available packages for PyPI repository ${repository.name}: ${repository.basicAuthUrlSimple.getSecure()}")
        }
    }

    suspend fun downloadDistributionsList(
        packageName: String,
        repository: PyPackageRepository
    ): List<PyDistributionInfo> {
        return httpClient.request<HttpStatement>(repository.basicAuthUrlSimple.join(packageName)).execute { response ->
            val distributionsPage = Jsoup.parse(response.readText())
            return@execute distributionsPage.body().getElementsByTag("a")
                .mapNotNull { PyDistributionInfo.fromString(it.text()) }
        }
    }

    suspend fun getDistributionUrl(
        distributionInfo: PyDistributionInfo,
        repository: PyPackageRepository
    ): PyPackageUrl? {
        return try {
            httpClient.request<HttpStatement>(repository.basicAuthUrlSimple.join(distributionInfo.name)).execute { response ->
                val distributionsPage = Jsoup.parse(response.readText())
                val element = distributionsPage.body().getElementsByTag("a")
                    .find { it.text() == distributionInfo.distributionFilename }
                return@execute element?.attr("href")
            }
        } catch (exception: Throwable) {
            error("Failed to resolve distribution ${distributionInfo.distributionFilename} in ${repository.basicAuthUrlSimple.getSecure()} due to network issues.")
        }
    }

    suspend fun downloadDistribution(url: PyPackageUrl, destination: File) {
        val httpResponse: HttpResponse = httpClient.get(url)
        val responseBody: ByteArray = httpResponse.receive()
        destination.writeBytes(responseBody)
    }

    suspend fun downloadMetadata(pkg: PyPackage, terminal: Terminal): JsonPackageMetadataInfo? {
        val metadataJsonUrl = pkg.repo.basicAuthUrl.join("pypi", pkg.name, pkg.version, "json")
        val response: HttpResponse = httpClient.request(metadataJsonUrl)
        return when (response.status) {
            HttpStatusCode.OK -> jsonParser.decodeFromString(response.readText())
            else -> {
                terminal.warn("Failed to download metadata for package ${pkg.name}==${pkg.version} from ${metadataJsonUrl.getSecure()}")
                terminal.warn("Http status: ${response.status}")
                null
            }
        }
    }
}
