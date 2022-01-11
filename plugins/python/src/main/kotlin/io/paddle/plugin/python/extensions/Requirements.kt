package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.dependencies.index.PyPackage
import io.paddle.plugin.python.utils.PyPackageName
import io.paddle.plugin.python.utils.PyPackageVersion
import io.paddle.project.Project
import io.paddle.utils.Hashable
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hashable


val Project.requirements: Requirements
    get() = extensions.get(Requirements.Extension.key)!!

class Requirements(val project: Project, val descriptors: MutableList<Descriptor>) : Hashable {

    val resolved: List<PyPackage> by lazy { descriptors.map { PyPackage.resolve(it, project) } }

    object Extension : Project.Extension<Requirements> {
        override val key: Extendable.Key<Requirements> = Extendable.Key()

        override fun create(project: Project): Requirements {
            val config = project.config.get<List<Map<String, String>>>("requirements") ?: emptyList()
            val descriptors = config.map { Descriptor(it["name"]!!, it["version"]!!, it["repository"]) }.toMutableList()
            return Requirements(project, descriptors)
        }
    }

    data class Descriptor(val name: PyPackageName, val version: PyPackageVersion, val repo: String?) : Hashable {
        val distInfoDirName = "${name}-${version}.dist-info"

        override fun hash(): String {
            val hashables = mutableListOf(name.hashable(), version.hashable())
            repo?.let { hashables.add(repo.hashable()) }
            return hashables.hashable().hash()
        }
    }

    override fun hash(): String {
        return descriptors.hashable().hash()
    }
}
