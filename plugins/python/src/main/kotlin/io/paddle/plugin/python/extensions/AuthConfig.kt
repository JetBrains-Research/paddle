package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.dependencies.authentication.AuthInfo
import io.paddle.plugin.python.dependencies.authentication.AuthType
import io.paddle.project.PaddleProject
import io.paddle.project.extensions.routeAsString
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import java.io.File

val PaddleProject.authConfig: AuthConfig
    get() = checkNotNull(extensions.get(AuthConfig.Extension.key)) { "Could not load extension AuthConfig for project $routeAsString" }

class AuthConfig private constructor(val project: PaddleProject, val file: File?, private val authInfosByRepoName: Map<String, List<AuthInfo>>) {
    companion object {
        const val FILENAME = "paddle.auth.yaml"
    }

    object Extension : PaddleProject.Extension<AuthConfig> {
        override val key: Extendable.Key<AuthConfig> = Extendable.Key()

        override fun create(project: PaddleProject): AuthConfig {
            val authConfigFile = project.rootDir.resolve(FILENAME).takeIf { it.exists() && it.readText().contains("repositories:") }
                ?: return AuthConfig(project, null, emptyMap())
            val config = object : ConfigurationView("repositories", from(authConfigFile)) {
                val authInfos by list<Map<String, String>>(name = "", default = emptyList())
            }

            val authInfosByRepoName = HashMap<String, MutableList<AuthInfo>>()
            for (authInfo in config.authInfos) {
                val repoName = authInfo["name"] ?: continue
                val authType = authInfo["type"] ?: continue
                val username = authInfo["username"] ?: continue
                authInfosByRepoName.getOrPut(repoName) { ArrayList() }.add(
                    AuthInfo.Config(type = AuthType.valueOf(authType.uppercase()), username = username)
                )
            }

            return AuthConfig(project, authConfigFile, authInfosByRepoName)
        }
    }

    fun findAuthInfos(repoName: String): List<AuthInfo> {
        return authInfosByRepoName[repoName] ?: emptyList()
    }
}
