package io.paddle.plugin.python.dependencies.authentication

import io.paddle.plugin.python.PyLocations
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.utils.*
import io.paddle.tasks.Task

enum class AuthType {
    KEYRING,
    NETRC,
    NONE,
    PROFILE
}

@kotlinx.serialization.Serializable
data class AuthInfo(
    val type: AuthType,
    val username: String? = null
) {
    companion object {
        val NONE = AuthInfo(AuthType.NONE)
    }
}

object AuthenticationProvider {
    private val netrc: NetrcConfig? by lazy { NetrcConfig.findInstance() }
    private val profiles: PaddleProfilesConfig? by lazy { PaddleProfilesConfig.getInstance() }

    fun resolveCredentials(repoUrl: PyPackagesRepositoryUrl, authInfo: AuthInfo): PyPackageRepository.Credentials {
        return when (authInfo.type) {
            AuthType.NETRC -> {
                netrc ?: throw Task.ActException(".netrc configuration not found.")
                netrc?.authenticators(host = repoUrl.getHost())
                    ?: throw Task.ActException("Can not find credentials for host = ${repoUrl.getHost()} within .netrc file.")
            }
            AuthType.KEYRING -> {
                val username = authInfo.username ?: throw IllegalStateException("Keyring auth: username must be specified.")
                val password = CachedKeyring.getCachedPasswordOrNull(repoUrl, username)
                    ?: CachedKeyring.getCachedPasswordOrNull(repoUrl.getSimple(), username)
                    ?: CachedKeyring.getCachedPasswordOrNull(repoUrl.getSimple().trim('/'), username)
                    ?: throw Task.ActException("Could not find appropriate credentials for host = $repoUrl, username = $username via Keyring.")
                return PyPackageRepository.Credentials(username, password)
            }
            AuthType.PROFILE -> {
                val username = authInfo.username ?: throw IllegalStateException("Profiles auth: username must be specified.")
                profiles ?: throw Task.ActException("File ${PyLocations.profiles.path} not found.")
                val token = profiles?.getTokenByNameOrNull(username)
                    ?: throw Task.ActException("Username $username was not found at ${PyLocations.profiles.path}")
                return PyPackageRepository.Credentials(username, token)
            }
            AuthType.NONE -> PyPackageRepository.Credentials.EMPTY
        }
    }
}
