package io.paddle.plugin.python.dependencies.index

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.paddle.plugin.python.dependencies.authentication.authProvider
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.index.metadata.JsonPackageMetadataInfo
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.utils.*
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.terminal.Terminal
import io.paddle.utils.ext.Extendable
import org.jsoup.Jsoup

val PaddleProject.webIndexer: PyPackageRepositoryIndexer
    get() = extensions.getOrFail(PyPackageRepositoryIndexer.Extension.key)

class PyPackageRepositoryIndexer(val project: PaddleProject) {
    object Extension : PaddleProject.Extension<PyPackageRepositoryIndexer> {
        override val key: Extendable.Key<PyPackageRepositoryIndexer> = Extendable.Key()

        override fun create(project: PaddleProject) = PyPackageRepositoryIndexer(project)
    }

    private val PyPackageRepository.credentials: PyPackageRepository.Credentials
        get() = project.authProvider.resolveCredentials(this)

    suspend fun downloadPackagesNames(repository: PyPackageRepository): Collection<PyPackageName> {
        val client = CachedHttpClient.getInstance(repository.credentials)
        val response = client.get(repository.urlSimple)
        val allNamesDocument = Jsoup.parse(response.bodyAsText())
        return allNamesDocument.body().getElementsByTag("a").map { it.text() }
    }

    suspend fun downloadDistributionsList(
        packageName: String,
        repository: PyPackageRepository
    ): List<PyDistributionInfo> {
        val client = CachedHttpClient.getInstance(repository.credentials)
        val response = client.get(repository.urlSimple.join(packageName.canonicalize()))
        val distributionsPage = Jsoup.parse(response.bodyAsText())
        return distributionsPage.body().getElementsByTag("a")
            .mapNotNull { PyDistributionInfo.fromString(it.text()) }
    }

    suspend fun getDistributionUrl(
        distributionInfo: PyDistributionInfo,
        repository: PyPackageRepository
    ): PyPackageUrl? {
        try {
            val client = CachedHttpClient.getInstance(repository.credentials)
            val response = client.get(repository.urlSimple.join(distributionInfo.name.canonicalize()))
            val distributionsPage = Jsoup.parse(response.bodyAsText())
            val element = distributionsPage.body().getElementsByTag("a")
                .find { it.text() == distributionInfo.distributionFilename }
            return element?.attr("href")
        } catch (exception: Throwable) {
            throw Task.ActException(
                "Failed to resolve distribution ${distributionInfo.distributionFilename} in " +
                        "${repository.urlSimple.getSecure()}: ${exception.message}."
            )
        }
    }

    suspend fun downloadMetadata(pkg: PyPackage, terminal: Terminal): JsonPackageMetadataInfo? {
        val metadataJsonUrl = pkg.repo.url.join("pypi", pkg.name, "json")
        val client = CachedHttpClient.getInstance(pkg.repo.credentials)
        val response = client.get(metadataJsonUrl)
        return when (response.status) {
            HttpStatusCode.OK -> jsonParser.decodeFromString(
                JsonPackageMetadataInfo.serializer(),
                response.bodyAsText()
            )

            else -> {
                terminal.warn("Failed to download metadata for package ${pkg.name}==${pkg.version} from ${metadataJsonUrl.getSecure()}")
                terminal.warn("Http status: ${response.status}")
                null
            }
        }
    }
}
