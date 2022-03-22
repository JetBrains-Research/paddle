package io.paddle.plugin.python.dependencies.authentication

import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.utils.PyPackagesRepositoryUrl
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
    val profile: String? = null
) {
    companion object {
        val NONE = AuthInfo(AuthType.NONE)
    }
}

object AuthenticationProvider {
    fun resolveCredentials(host: PyPackagesRepositoryUrl, authInfo: AuthInfo): PyPackageRepository.Credentials {
        return when (authInfo.type) {
            AuthType.NETRC -> {
                NetrcConfig.find().authenticators(host = host)
                    ?: throw Task.ActException("Can not find credentials for host = $host within .netrc file.")
            }
            else -> PyPackageRepository.Credentials.EMPTY
        }
    }
}
