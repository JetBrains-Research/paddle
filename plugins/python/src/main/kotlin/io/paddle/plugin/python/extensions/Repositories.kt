package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.dependencies.authentication.AuthInfo
import io.paddle.plugin.python.dependencies.authentication.AuthType
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepositories
import io.paddle.plugin.python.utils.*
import io.paddle.project.PaddleProject
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.*
import kotlin.system.measureTimeMillis


val PaddleProject.repositories: Repositories
    get() = extensions.get(Repositories.Extension.key)!!

class Repositories(val project: PaddleProject, val descriptors: List<Descriptor>) : Hashable {

    val resolved: PyPackageRepositories by lazy {
        project.terminal.info("Resolving repositories...")
        val result: PyPackageRepositories
        measureTimeMillis {
            result = PyPackageRepositories.resolve(descriptors, project)
        }.also {
            project.terminal.info("Finished resolving repositories: $it ms")
        }
        result
    }

    @Suppress("UNCHECKED_CAST")
    object Extension : PaddleProject.Extension<Repositories> {
        override val key: Extendable.Key<Repositories> = Extendable.Key()

        override fun create(project: PaddleProject): Repositories {
            val reposConfig = project.config.get<List<Map<String, Any>>>("repositories") ?: emptyList()

            val descriptors = reposConfig.map { repo ->
                val authDescriptor: Map<String, String> = repo["auth"] as? Map<String, String> ?: emptyMap()
                val type = AuthType.valueOf((authDescriptor["type"] ?: "none").uppercase())
                val username = authDescriptor["username"]

                val url = repo["url"]!! as String
                val uploadUrl = repo["uploadUrl"] as String?

                Descriptor(
                    name = repo["name"]!! as String,
                    url = url,
                    default = (repo["default"] as String?)?.toBoolean(),
                    secondary = (repo["secondary"] as String?)?.toBoolean(),
                    authInfo = AuthInfo(type, username),
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
        val authInfo: AuthInfo,
        val uploadUrl: PyPackagesRepositoryUrl = url.removeSimple().join("legacy"),
    ) : Hashable {
        override fun hash(): String {
            val hashables = (listOf(name.hashable(), url.hashable()) + authInfo).toMutableList()
            default?.let { hashables.add(it.hashable()) }
            secondary?.let { hashables.add(it.hashable()) }
            return hashables.hashable().hash()
        }

        companion object {
            val PYPI = Descriptor(
                name = "pypi",
                url = "https://pypi.org",
                default = true,
                secondary = false,
                authInfo = AuthInfo.NONE,
                uploadUrl = "https://upload.pypi.org/legacy/"
            )
        }
    }

    override fun hash(): String {
        return descriptors.hashable().hash()
    }
}
