package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.dependencies.authentication.AuthInfo
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepositories
import io.paddle.plugin.python.utils.*
import io.paddle.project.PaddleProject
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable


val PaddleProject.repositories: Repositories
    get() = extensions.get(Repositories.Extension.key)!!

class Repositories(val project: PaddleProject, val descriptors: List<Descriptor>) : Hashable {

    val resolved: PyPackageRepositories by lazy {
        PyPackageRepositories.resolve(descriptors, project)
    }

    object Extension : PaddleProject.Extension<Repositories> {
        override val key: Extendable.Key<Repositories> = Extendable.Key()

        override fun create(project: PaddleProject): Repositories {
            val reposConfig = project.config.get<List<Map<String, Any>>>("repositories") ?: emptyList()

            val descriptors = reposConfig.map { repo ->
                val repoName = checkNotNull(repo["name"]) {
                    "Failed to parse ${project.buildFile.canonicalPath}: <name> must be specified for each repository."
                } as String
                val url = checkNotNull(repo["url"]) {
                    "Failed to parse ${project.buildFile.canonicalPath}: <url> must be specified for each repository."
                } as String
                val uploadUrl = repo["uploadUrl"] as String?

                val authInfos = project.authConfig.findAuthInfos(repoName).takeIf { it.isNotEmpty() }
                    ?: listOf(AuthInfo.NONE).also {
                        project.terminal.info(
                            "Authentication method for PyPI repo $repoName is not provided " +
                                "in ${project.authConfig.file.canonicalPath}, proceeding..."
                        )
                    }

                Descriptor(
                    name = repoName,
                    url = url,
                    default = (repo["default"] as String?)?.toBoolean(),
                    secondary = (repo["secondary"] as String?)?.toBoolean(),
                    authInfos = authInfos,
                    uploadUrl = uploadUrl ?: url.removeSimple().join("legacy")
                )
            }

            return Repositories(project, descriptors)
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
        return descriptors.hashable().hash()
    }
}
