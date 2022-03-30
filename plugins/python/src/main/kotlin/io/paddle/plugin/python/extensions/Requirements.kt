package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.dependencies.resolvers.PipResolver
import io.paddle.plugin.python.utils.PyPackageName
import io.paddle.plugin.python.utils.PyPackageVersion
import io.paddle.project.Project
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable


val Project.requirements: Requirements
    get() = extensions.get(Requirements.Extension.key)!!

class Requirements(val project: Project, val descriptors: MutableList<Descriptor>) : Hashable {

    val resolved: Collection<PyPackage> by lazy {
        PipResolver.resolve(project) + project.environment.venv.pyPackages
    }

    object Extension : Project.Extension<Requirements> {
        override val key: Extendable.Key<Requirements> = Extendable.Key()

        override fun create(project: Project): Requirements {
            val config = project.config.get<List<Map<String, String>>>("requirements") ?: emptyList()
            val descriptors = config.map { Descriptor(it["name"]!!, it["version"], it["repository"]) }.toMutableList()
            return Requirements(project, descriptors)
        }
    }

    data class Descriptor(val name: PyPackageName, val version: PyPackageVersion? = null, val repo: String? = null) : Hashable {
        override fun hash(): String {
            val hashables = mutableListOf(name.hashable())
            version?.let { hashables.add(version.hashable()) }
            repo?.let { hashables.add(repo.hashable()) }
            return hashables.hashable().hash()
        }
    }

    override fun hash(): String {
        return descriptors.hashable().hash()
    }
}
