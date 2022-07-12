package io.paddle.plugin.python.dependencies.repositories

import io.paddle.plugin.python.PyLocations
import io.paddle.plugin.python.dependencies.authentication.AuthInfo
import io.paddle.plugin.python.dependencies.authentication.AuthenticationProvider
import io.paddle.plugin.python.dependencies.index.PyPackageRepositoryIndexer
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.index.wordlist.PackedWordList
import io.paddle.plugin.python.dependencies.index.wordlist.PackedWordListSerializer
import io.paddle.plugin.python.extensions.Repositories
import io.paddle.plugin.python.utils.*
import io.paddle.utils.hash.*
import kotlinx.serialization.*
import java.io.File

@Serializable
class PyPackageRepository(val url: PyPackagesRepositoryUrl, val name: String, val authInfo: AuthInfo, val uploadUrl: PyPackagesRepositoryUrl) {
    constructor(metadata: Metadata) : this(metadata.url, metadata.name, metadata.authInfo, metadata.uploadUrl)
    constructor(descriptor: Repositories.Descriptor) : this(descriptor.url.removeSimple(), descriptor.name, descriptor.authInfo, descriptor.uploadUrl)

    @Serializable
    data class Metadata(val url: PyPackagesRepositoryUrl, val name: String, val authInfo: AuthInfo, val uploadUrl: PyPackagesRepositoryUrl) : Hashable {
        override fun hash() = listOf(url, name, authInfo.username.toString(), authInfo.type.toString()).map { it.hashable() }.hashable().hash()
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

    val authenticatedUrl: PyPackagesRepositoryUrl
        get() = credentials.authenticate(url)

    @Transient
    val urlSimple: PyPackagesRepositoryUrl = url.join("simple")

    val authenticatedUrlSimple: PyPackagesRepositoryUrl
        get() = credentials.authenticate(urlSimple)

    val credentials: Credentials
        get() = AuthenticationProvider.resolveCredentials(url, authInfo)

    val metadata = Metadata(url, name, authInfo, uploadUrl)

    companion object {
        val PYPI_REPOSITORY = PyPackageRepository(Repositories.Descriptor.PYPI)
    }

    // Index is loaded from cache
    @Serializable(with = PackedWordListSerializer::class)
    private var packagesNamesCache: PackedWordList = PackedWordList.empty

    // Index is loaded from cache
    private val distributionsCache: MutableMap<PyPackageName, List<PyDistributionInfo>> = HashMap()

    @Transient
    val cacheFileName: String = "repo_" + StringHashable(url).hash() + ".json"

    suspend fun updateIndex() {
        val names = try {
            PyPackageRepositoryIndexer.downloadPackagesNames(this)
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
        val cachedCopy: PyPackageRepository = jsonParser.decodeFromString(file.readText())
        packagesNamesCache = cachedCopy.packagesNamesCache
    }

    fun getPackagesNamesByPrefix(prefix: String): Sequence<PyPackageName> = packagesNamesCache.prefix(prefix)

    suspend fun findAvailableDistributionsByPackageName(packageName: PyPackageName, useCache: Boolean = true): List<PyDistributionInfo> {
        if (useCache && packageName in distributionsCache) {
            return distributionsCache[packageName]!!
        }

        val distributions = try {
            PyPackageRepositoryIndexer.downloadDistributionsList(packageName, this)
        } catch (e: Throwable) {
            throw IndexUpdateException("Failed to download distributions list for package $packageName from repository ${urlSimple.getSecure()}.")
        }

        return distributions.also { if (useCache) distributionsCache[packageName] = it }
    }

    fun saveCache() {
        PyLocations.indexDir.resolve(this.cacheFileName).toFile()
            .writeText(jsonParser.encodeToString(this))
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
