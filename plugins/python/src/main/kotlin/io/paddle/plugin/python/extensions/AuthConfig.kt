package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.dependencies.authentication.AuthInfo
import io.paddle.plugin.python.dependencies.authentication.AuthType
import io.paddle.project.PaddleProject
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import java.io.File

val PaddleProject.authConfig: AuthConfig
    get() = extensions.get(AuthConfig.Extension.key)!!

class AuthConfig private constructor(val project: PaddleProject, val file: File?, private val authInfosByRepoName: Map<String, List<AuthInfo>>) {
    companion object {
        const val FILENAME = "paddle.auth.yaml"
    }

    object Extension : PaddleProject.Extension<AuthConfig> {
        override val key: Extendable.Key<AuthConfig> = Extendable.Key()

        override fun create(project: PaddleProject): AuthConfig {
            val authConfigFile = project.rootDir.resolve(FILENAME).takeIf { it.exists() }
                ?: return AuthConfig(project, null, emptyMap())
            val config = object : ConfigurationView("repositories", from(authConfigFile)) {
                val authInfos by list<Map<String, String>>(name = "", default = emptyList())
            }

            val authInfosByRepoName = HashMap<String, MutableList<AuthInfo>>()
            for (authInfo in config.authInfos) {
                val repoName = checkNotNull(authInfo["name"]) { "Failed to parse $FILENAME: <name> field must be specified for each repository." }
                val authType = checkNotNull(authInfo["type"]) { "Failed to parse $FILENAME: <type> field must be specified for each repository." }
                val username = checkNotNull(authInfo["username"]) { "Failed to parse $FILENAME: <username> field must be specified for each repository." }
                authInfosByRepoName.getOrPut(repoName) { ArrayList() }.add(
                    AuthInfo(type = AuthType.valueOf(authType.uppercase()), username = username)
                )
            }

            return AuthConfig(project, authConfigFile, authInfosByRepoName)
        }
    }

    fun findAuthInfos(repoName: String): List<AuthInfo> {
        return authInfosByRepoName[repoName] ?: emptyList()
    }
}
