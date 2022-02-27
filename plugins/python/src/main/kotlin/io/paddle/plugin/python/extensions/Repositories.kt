package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.dependencies.repositories.PyPackageRepositories
import io.paddle.plugin.python.utils.PyPackagesRepositoryUrl
import io.paddle.project.Project
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable


val Project.repositories: Repositories
    get() = extensions.get(Repositories.Extension.key)!!

class Repositories(val project: Project, val descriptors: List<Descriptor>) : Hashable {

    val resolved: PyPackageRepositories by lazy { PyPackageRepositories.resolve(descriptors) }

    object Extension : Project.Extension<Repositories> {
        override val key: Extendable.Key<Repositories> = Extendable.Key()

        override fun create(project: Project): Repositories {
            val config = project.config.get<List<Map<String, String>>>("repositories") ?: emptyList()

            val descriptors = config.map {
                Descriptor(
                    it["name"]!!,
                    it["url"]!!,
                    it["default"].toBoolean(),
                    it["secondary"].toBoolean()
                )
            }

            return Repositories(project, descriptors)
        }
    }

    data class Descriptor(
        val name: String,
        val url: PyPackagesRepositoryUrl,
        val default: Boolean?,
        val secondary: Boolean?
    ) : Hashable {
        override fun hash(): String {
            val hashables = mutableListOf(name.hashable(), url.hashable())
            default?.let { hashables.add(it.hashable()) }
            secondary?.let { hashables.add(it.hashable()) }
            return hashables.hashable().hash()
        }

        companion object {
            val PYPI = Descriptor("pypi", "", default = true, secondary = false)
        }
    }

    override fun hash(): String {
        return descriptors.hashable().hash()
    }
}
