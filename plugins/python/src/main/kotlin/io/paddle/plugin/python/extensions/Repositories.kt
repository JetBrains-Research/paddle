package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.dependencies.authentication.AuthInfo
import io.paddle.plugin.python.dependencies.authentication.AuthType
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepositories
import io.paddle.plugin.python.hasPython
import io.paddle.plugin.python.utils.*
import io.paddle.project.PaddleProject
import io.paddle.project.extensions.routeAsString
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable


val PaddleProject.repositories: Repositories
    get() = checkNotNull(extensions.get(Repositories.Extension.key)) { "Could not load extension Repositories for project $routeAsString" }

class Repositories(val project: PaddleProject, val descriptors: List<Descriptor>, val findLinks: List<String>) : Hashable {

    val resolved: PyPackageRepositories by lazy {
        PyPackageRepositories.resolve(descriptors, project, findLinks)
    }

    object Extension : PaddleProject.Extension<Repositories> {
        override val key: Extendable.Key<Repositories> = Extendable.Key()

        @Suppress("UNCHECKED_CAST")
        override fun create(project: PaddleProject): Repositories {
            val reposConfig = project.config.get<List<Map<String, Any>>>("repositories") ?: emptyList()
            val findLinks = project.config.get<List<String>>("findLinks") ?: emptyList()

            val descriptors = reposConfig.map { repo ->
                val repoName = checkNotNull(repo["name"]) {
                    "Failed to parse ${project.buildFile.canonicalPath}: <name> must be specified for each repository."
                } as String
                val url = checkNotNull(repo["url"]) {
                    "Failed to parse ${project.buildFile.canonicalPath}: <url> must be specified for each repository."
                } as String
                val uploadUrl = repo["uploadUrl"] as String?

                val authInfos = project.authConfig.findAuthInfos(repoName).takeIf { it.isNotEmpty() }
                    ?: mutableListOf(AuthInfo.NONE).also {
                        project.authConfig.file ?: run {
                            project.terminal.info("${AuthConfig.FILENAME} was not found, proceeding...")
                        }
                    }

                val fallbackAuthInfo = (repo["authEnv"] as? Map<String, String>)?.let { auth ->
                    AuthInfo.Env(
                        usernameVar = checkNotNull(auth["username"]) { "authEnv.username must be specified" },
                        passwordVar = checkNotNull(auth["password"]) { "authEnv.password must be specified" },
                    )
                }

                Descriptor(
                    name = repoName,
                    url = url,
                    default = (repo["default"] as String?)?.toBoolean(),
                    secondary = (repo["secondary"] as String?)?.toBoolean(),
                    authInfos = fallbackAuthInfo?.let {
                        authInfos.filter { info -> info.type != AuthType.NONE } + it
                    } ?: authInfos,
                    uploadUrl = uploadUrl ?: url.removeSimple().join("legacy")
                )
            }

            return Repositories(project, descriptors, findLinks)
        }
    }

    data class Descriptor(
        val name: String,
        val url: PyPackagesRepositoryUrl,
        val default: Boolean?,
        val secondary: Boolean?,
        val authInfos: List<AuthInfo>,
        val uploadUrl: PyPackagesRepositoryUrl = url.removeSimple().join("legacy"),
    ) : Hashable {
        override fun hash(): String {
            val hashables = mutableListOf(name.hashable(), url.hashable())
            default?.let { hashables.add(it.hashable()) }
            secondary?.let { hashables.add(it.hashable()) }
            return listOf(hashables.hashable(), authInfos.hashable()).hashable().hash()
        }

        companion object {
            val PYPI = Descriptor(
                name = "pypi",
                url = "https://pypi.org",
                default = true,
                secondary = false,
                authInfos = listOf(AuthInfo.NONE),
                uploadUrl = "https://upload.pypi.org/legacy/"
            )
        }
    }

    override fun hash(): String {
        return (descriptors + findLinks.map { it.hashable() }).hashable().hash()
    }
}

fun PaddleProject.getAllPyPackageRepoDescriptors(): Set<Repositories.Descriptor> {
    val result = hashSetOf(Repositories.Descriptor.PYPI)
    if (hasPython) result += repositories.descriptors

    return result + subprojects.filter { it.hasPython }.flatMap { it.getAllPyPackageRepoDescriptors() }.toSet()
}
