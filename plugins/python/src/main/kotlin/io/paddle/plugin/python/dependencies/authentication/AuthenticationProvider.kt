package io.paddle.plugin.python.dependencies.authentication

import com.github.javakeyring.Keyring
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.extensions.pyLocations
import io.paddle.plugin.python.utils.PyPackagesRepositoryUrl
import io.paddle.plugin.python.utils.getHost
import io.paddle.plugin.python.utils.getSimple
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AuthType {
    ENV,
    KEYRING,
    NETRC,
    NONE,
    PROFILE
}

@Serializable
sealed class AuthInfo : Hashable {
    @SerialName("authType")
    abstract val type: AuthType

    companion object {
        val NONE = Config(AuthType.NONE)
    }

    @Serializable
    data class Config(
        @SerialName("authTypeConfig") override val type: AuthType,
        val username: String? = null
    ) : Hashable, AuthInfo() {
        override fun hash(): String {
            return listOf(type.toString().hashable(), (username ?: "").hashable()).hashable().hash()
        }
    }

    @Serializable
    data class Env(
        val usernameVar: String,
        val passwordVar: String
    ) : Hashable, AuthInfo() {
        @SerialName("authTypeEnv")
        override val type = AuthType.ENV
        override fun hash(): String {
            return listOf(usernameVar.hashable(), passwordVar.hashable()).hashable().hash()
        }
    }
}

val PaddleProject.authProvider: AuthenticationProvider
    get() = extensions.getOrFail(AuthenticationProvider.Extension.key)

class AuthenticationProvider private constructor(val project: PaddleProject) {
    object Extension : PaddleProject.Extension<AuthenticationProvider> {
        override val key: Extendable.Key<AuthenticationProvider> = Extendable.Key()

        override fun create(project: PaddleProject): AuthenticationProvider {
            return AuthenticationProvider(project)
        }
    }

    private val netrc: NetrcConfig?
        get() = NetrcConfig.findInstance(project.executor.env)

    private val profiles: PaddleProfilesConfig?
        get() = project.pyLocations.profiles.toFile()
            .takeIf { it.exists() }
            ?.let { PaddleProfilesConfig.getInstance(it) }

    fun resolveCredentials(repo: PyPackageRepository): PyPackageRepository.Credentials =
        resolveCredentials(repo.url, repo.authInfos)

    fun resolveCredentials(
        repoUrl: PyPackagesRepositoryUrl,
        authInfos: List<AuthInfo>
    ): PyPackageRepository.Credentials {
        for (authInfo in authInfos) {
            when (authInfo.type) {
                AuthType.ENV -> {
                    val (usernameVar, passwordVar) = (authInfo as AuthInfo.Env)
                    project.executor.env.get(usernameVar)?.let { username ->
                        project.executor.env.get(passwordVar)?.let { password ->
                            return PyPackageRepository.Credentials(login = username, password = password)
                        } ?: project.terminal.error("Can not find password env variable: $passwordVar")
                    } ?: project.terminal.error("Can not find username env variable: $usernameVar")
                }

                AuthType.NETRC -> {
                    netrc ?: project.terminal.error(".netrc configuration not found.")
                    netrc?.authenticators(host = repoUrl.getHost())?.also { return it }
                        ?: project.terminal.error("Can not find credentials for host = ${repoUrl.getHost()} within .netrc file.")
                }

                AuthType.KEYRING -> {
                    val keyring = Keyring.create()
                    val usernameOrNull = (authInfo as AuthInfo.Config).username ?: (null).also {
                        project.terminal.error("Keyring authentication error: username must be specified.")
                    }
                    usernameOrNull?.let { username ->
                        val passwordOrNull = keyring.getPassword(repoUrl, username)
                            ?: keyring.getPassword(repoUrl.getSimple(), username)
                            ?: keyring.getPassword(repoUrl.getSimple().trim('/'), username)
                            ?: (null).also {
                                project.terminal.error("Could not find appropriate credentials for host = $repoUrl, username = $username via Keyring.")
                            }
                        passwordOrNull?.let { password ->
                            return PyPackageRepository.Credentials(username, password)
                        }
                    }
                }

                AuthType.PROFILE -> {
                    val usernameOrNull = (authInfo as AuthInfo.Config).username ?: (null).also {
                        project.terminal.error("Profiles auth: username must be specified.")
                    }
                    usernameOrNull?.let { username ->
                        profiles ?: project.terminal.error("File ${project.pyLocations.profiles} not found.")
                        val tokenOrNull = profiles?.getTokenByNameOrNull(username)
                            ?: (null).also { project.terminal.error("Username $username was not found at ${project.pyLocations.profiles}") }
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
