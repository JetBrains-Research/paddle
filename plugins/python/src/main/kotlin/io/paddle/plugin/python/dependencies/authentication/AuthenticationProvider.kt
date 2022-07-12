package io.paddle.plugin.python.dependencies.authentication

import com.github.javakeyring.Keyring
import io.paddle.plugin.python.PyLocations
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.utils.*
import io.paddle.tasks.Task
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable

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
) : Hashable {
    companion object {
        val NONE = AuthInfo(AuthType.NONE)
    }

    override fun hash(): String {
        return listOf(type.toString().hashable(), (username ?: "").hashable()).hashable().hash()
    }
}

object AuthenticationProvider {
    private val netrc: NetrcConfig?
        get() = NetrcConfig.findInstance()

    private val profiles: PaddleProfilesConfig?
        get() = PaddleProfilesConfig.getInstance()

    fun resolveCredentials(repoUrl: PyPackagesRepositoryUrl, authInfo: AuthInfo): PyPackageRepository.Credentials {
        return when (authInfo.type) {
            AuthType.NETRC -> {
                netrc ?: throw Task.ActException(".netrc configuration not found.")
                netrc?.authenticators(host = repoUrl.getHost())
                    ?: throw Task.ActException("Can not find credentials for host = ${repoUrl.getHost()} within .netrc file.")
            }

            AuthType.KEYRING -> {
                val keyring = Keyring.create()
                val username = authInfo.username ?: throw IllegalStateException("Keyring auth: username must be specified.")
                val password = keyring.getPassword(repoUrl, username)
                    ?: keyring.getPassword(repoUrl.getSimple(), username)
                    ?: keyring.getPassword(repoUrl.getSimple().trim('/'), username)
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
