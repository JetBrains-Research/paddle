package io.paddle.plugin.python.dependencies.authentication

import io.paddle.plugin.python.PyLocations
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.utils.PyPackagesRepositoryUrl
import io.paddle.plugin.python.utils.getSimple
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

    fun resolveCredentials(host: PyPackagesRepositoryUrl, authInfo: AuthInfo): PyPackageRepository.Credentials {
        return when (authInfo.type) {
            AuthType.NETRC -> {
                netrc ?: throw Task.ActException(".netrc configuration not found.")
                netrc?.authenticators(host = host)
                    ?: throw Task.ActException("Can not find credentials for host = $host within .netrc file.")
            }
            AuthType.KEYRING -> {
                val username = authInfo.username ?: throw IllegalStateException("Keyring auth: username must be specified.")
                val password = CachedKeyring.getCachedPasswordOrNull(host, username)
                    ?: CachedKeyring.getCachedPasswordOrNull(host.getSimple(), username)
                    ?: CachedKeyring.getCachedPasswordOrNull(host.getSimple().trim('/'), username)
                    ?: throw Task.ActException("Could not find appropriate credentials for host = $host, username = $username via Keyring.")
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
