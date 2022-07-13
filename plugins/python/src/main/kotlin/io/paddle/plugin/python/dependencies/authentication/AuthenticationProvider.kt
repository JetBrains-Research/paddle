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

    fun resolveCredentials(repoUrl: PyPackagesRepositoryUrl, authInfos: List<AuthInfo>): PyPackageRepository.Credentials {
        for (authInfo in authInfos) {
            when (authInfo.type) {
                AuthType.NETRC -> {
                    netrc ?: PaddleLogger.terminal.error(".netrc configuration not found.")
                    netrc?.authenticators(host = repoUrl.getHost())?.also { return it }
                        ?: PaddleLogger.terminal.error("Can not find credentials for host = ${repoUrl.getHost()} within .netrc file.")
                }

                AuthType.KEYRING -> {
                    val keyring = Keyring.create()
                    val usernameOrNull = authInfo.username ?: (null).also {
                        PaddleLogger.terminal.error("Keyring authentication error: username must be specified.")
                    }
                    usernameOrNull?.let { username ->
                        val passwordOrNull = keyring.getPassword(repoUrl, username)
                            ?: keyring.getPassword(repoUrl.getSimple(), username)
                            ?: keyring.getPassword(repoUrl.getSimple().trim('/'), username)
                            ?: (null).also {
                                PaddleLogger.terminal.error("Could not find appropriate credentials for host = $repoUrl, username = $username via Keyring.")
                            }
                        passwordOrNull?.let { password ->
                            return PyPackageRepository.Credentials(username, password)
                        }
                    }
                }

                AuthType.PROFILE -> {
                    val usernameOrNull = authInfo.username ?: (null).also {
                        PaddleLogger.terminal.error("Profiles auth: username must be specified.")
                    }
                    usernameOrNull?.let { username ->
                        profiles ?: PaddleLogger.terminal.error("File ${PyLocations.profiles.path} not found.")
                        val tokenOrNull = profiles?.getTokenByNameOrNull(username)
                            ?: (null).also { PaddleLogger.terminal.error("Username $username was not found at ${PyLocations.profiles.path}") }
                        tokenOrNull?.let { token ->
                            return PyPackageRepository.Credentials(username, token)
                        }
                    }
                }

                AuthType.NONE -> return PyPackageRepository.Credentials.EMPTY
            }
        }

        throw Task.ActException("Could not resolve any credentials for repository $repoUrl")
    }
}
