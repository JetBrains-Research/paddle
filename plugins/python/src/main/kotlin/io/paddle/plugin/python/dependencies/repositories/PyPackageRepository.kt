package io.paddle.plugin.python.dependencies.repositories

import io.paddle.plugin.python.dependencies.authentication.AuthInfo
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.index.webIndexer
import io.paddle.plugin.python.dependencies.index.wordlist.PackedWordList
import io.paddle.plugin.python.dependencies.index.wordlist.PackedWordListSerializer
import io.paddle.plugin.python.extensions.Repositories
import io.paddle.plugin.python.extensions.pyLocations
import io.paddle.plugin.python.utils.*
import io.paddle.project.PaddleProject
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.StringHashable
import io.paddle.utils.hash.hashable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File

@Serializable
class PyPackageRepository(
    val url: PyPackagesRepositoryUrl,
    val name: String,
    val authInfos: List<AuthInfo>,
    val uploadUrl: PyPackagesRepositoryUrl
) {
    constructor(metadata: Metadata) : this(metadata.url, metadata.name, metadata.authInfos, metadata.uploadUrl)
    constructor(descriptor: Repositories.Descriptor) : this(
        descriptor.url.removeSimple(),
        descriptor.name,
        descriptor.authInfos,
        descriptor.uploadUrl
    )

    @Serializable
    data class Metadata(
        val url: PyPackagesRepositoryUrl,
        val name: String,
        val authInfos: List<AuthInfo>,
        val uploadUrl: PyPackagesRepositoryUrl
    ) : Hashable {
        override fun hash() = listOf(url.hashable(), name.hashable(), authInfos.hashable()).hashable().hash()
    }

    /**
     * Credentials for PyPi repository, used via Basic Auth.
     *
     * @param account the second password (not used for now)
     */
    open class Credentials(val login: String, val password: String, val account: String? = null) {
        companion object {
            val EMPTY = Empty()
        }

        open val urlPrefix: String
            get() = "$login:$password@"

        fun authenticate(url: PyUrl): PyUrl {
            val (protocol, suffix) = url.split("://")
            return "$protocol://$urlPrefix$suffix"
        }

        class Empty : Credentials("", "") {
            override val urlPrefix: String
                get() = ""
        }
    }

    @Transient
    val urlSimple: PyPackagesRepositoryUrl = url.join("simple")

    val metadata = Metadata(url, name, authInfos, uploadUrl)

    companion object {
        val PYPI_REPOSITORY = PyPackageRepository(Repositories.Descriptor.PYPI)
    }

    // Index is loaded from cache
    @Serializable(with = PackedWordListSerializer::class)
    private var packagesNamesCache: PackedWordList = PackedWordList.empty

    // Index is loaded from cache
    private val distributionsCache: MutableMap<PyPackageName, List<PyDistributionInfo>> = HashMap()

    @Transient
    val uid: String = "repo_" + StringHashable(url).hash()

    @Transient
    val cacheFileName: String = "$uid.json"

    suspend fun updateIndex(project: PaddleProject) {
        val names = try {
            project.webIndexer.downloadPackagesNames(this)
        } catch (e: Throwable) {
            throw IndexUpdateException("Failed to update index for repository ${urlSimple.getSecure()}.")
        }
        if (names.isEmpty()) {
            throw IndexUpdateException(
                "Downloaded index for repository ${urlSimple.getSecure()} is empty. " +
                        "It is either unavailable at he moment or corrupted."
            )
        }
        packagesNamesCache = PackedWordList(names.toSet())
    }

    fun loadCache(file: File) {
        require(file.name == this.cacheFileName)
        val cachedCopy: PyPackageRepository = jsonParser.decodeFromString(serializer(), file.readText())
        packagesNamesCache = cachedCopy.packagesNamesCache
    }

    fun getPackagesNamesByPrefix(prefix: String): Sequence<PyPackageName> = packagesNamesCache.prefix(prefix)

    suspend fun findAvailableDistributionsByPackageName(
        packageName: PyPackageName,
        project: PaddleProject,
        useCache: Boolean = true
    ): List<PyDistributionInfo> {
        if (useCache && packageName in distributionsCache) {
            return distributionsCache[packageName]!!
        }

        val distributions = try {
            project.webIndexer.downloadDistributionsList(packageName, this)
        } catch (e: Throwable) {
            throw IndexUpdateException("Failed to download distributions list for package $packageName from repository ${urlSimple.getSecure()}.")
        }

        return distributions.also { if (useCache) distributionsCache[packageName] = it }
    }

    fun saveCache(project: PaddleProject) {
        project.pyLocations.indexDir.resolve(this.cacheFileName).toFile()
            .writeText(jsonParser.encodeToString(serializer(), this))
    }

    override fun hashCode() = metadata.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PyPackageRepository

        return metadata == other.metadata
    }
}

class IndexUpdateException(reason: String) : Exception(reason)
