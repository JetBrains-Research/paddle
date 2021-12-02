package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.dependencies.index.PyDistributionsResolver
import io.paddle.plugin.python.dependencies.index.PyPackagesRepositories
import io.paddle.plugin.python.dependencies.index.PyPackagesRepository
import io.paddle.plugin.python.dependencies.index.utils.PyPackageName
import io.paddle.plugin.python.dependencies.index.utils.PyPackageUrl
import io.paddle.plugin.python.dependencies.index.utils.PyPackageVersion
import io.paddle.project.Project
import io.paddle.utils.Hashable
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hashable

val Project.requirements: Requirements
    get() = extensions.get(Requirements.Extension.key)!!

class Requirements(val descriptors: MutableList<Descriptor>, val repositories: PyPackagesRepositories) : Hashable {
    object Extension : Project.Extension<Requirements> {
        override val key: Extendable.Key<Requirements> = Extendable.Key()

        override fun create(project: Project): Requirements {
            val config = object : ConfigurationView("requirements", project.config) {
                val libraries by list<Map<String, String>>("libraries", default = emptyList())
                val repositories by list<Map<String, String>>("repositories", default = emptyList())
            }

            val repositories = PyPackagesRepositories.parse(config.repositories)
            val descriptors = config.libraries.map { Descriptor.resolve(it["name"]!!, it["version"]!!, repositories) }.toMutableList()

            return Requirements(descriptors, repositories)
        }
    }

    data class Descriptor(
        val name: PyPackageName,
        val version: PyPackageVersion,
        val url: PyPackageUrl,
        val repo: PyPackagesRepository
    ) : Hashable {
        val distInfoDirName = "${name}-${version}.dist-info"

        companion object {
            fun resolve(name: PyPackageName, version: PyPackageVersion, repositories: PyPackagesRepositories): Descriptor {
                val url = PyDistributionsResolver.resolve(name, version, repositories) // TODO: implement resolver
                val repo = repositories.getRepositoryByPyPackageUrl(url)
                return Descriptor(name, version, url, repo)
            }

            fun resolve(name: PyPackageName, version: PyPackageVersion, repo: PyPackagesRepository): Descriptor {
                val url = PyDistributionsResolver.resolve(name, version, repo) // TODO: implement resolver
                return Descriptor(name, version, url, repo)
            }
        }

        override fun hash(): String {
            return listOf(name.hashable(), version.hashable(), url.hashable()).hashable().hash()
        }
    }

    override fun hash(): String {
        return descriptors.hashable().hash()
    }
}
