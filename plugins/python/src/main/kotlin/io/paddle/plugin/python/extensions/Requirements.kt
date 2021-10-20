package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.dependencies.index.PyPackagesRepositories
import io.paddle.plugin.python.dependencies.index.PyPackagesRepository
import io.paddle.plugin.python.dependencies.isValidUrl
import io.paddle.project.Project
import io.paddle.utils.Hashable
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hashable
import java.io.File

val Project.requirements: Requirements
    get() = extensions.get(Requirements.Extension.key)!!

class Requirements(val descriptors: MutableList<Descriptor>, val files: MutableList<File>, val repositories: PyPackagesRepositories) : Hashable {
    object Extension : Project.Extension<Requirements> {
        override val key: Extendable.Key<Requirements> = Extendable.Key()

        override fun create(project: Project): Requirements {
            val config = object : ConfigurationView("requirements", project.config) {
                val requirementsFile by string("file", default = "requirements.txt")
                val libraries by list<Map<String, String>>("libraries", default = emptyList())
                val repositories by list<Map<String, String>>("repositories", default = emptyList())
            }

            val libraries = config.libraries.map { Descriptor(it["name"]!!, it["version"]!!) }.toMutableList()
            val repositories = PyPackagesRepositories(
                repositories = config.repositories.map {
                    // TODO: design a better way of parameters validation
                    val (url, name) = it["url"]!! to it["name"]!!
                    require(url.isValidUrl()) { "The specified URL=$url is not valid." }

                    PyPackagesRepository(
                        url = url.removeSuffix("/").removeSuffix("/simple"),
                        name = name
                    )
                }.toMutableSet(),
                useCached = true,
                updateAllIndex = false
            )

            return Requirements(libraries, mutableListOf(File(project.workDir, config.requirementsFile)), repositories)
        }
    }

    data class Descriptor(val name: String, val version: String) : Hashable {
        val distInfoDirName = "${name}-${version}.dist-info"

        override fun hash(): String {
            return listOf(name.hashable(), version.hashable()).hashable().hash()
        }
    }

    override fun hash(): String {
        return (files.map { it.hashable() } + descriptors).hashable().hash()
    }
}
