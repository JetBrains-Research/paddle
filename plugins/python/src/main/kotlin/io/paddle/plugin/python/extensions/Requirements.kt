package io.paddle.plugin.python.extensions

import io.paddle.project.Project
import io.paddle.utils.Hashable
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hashable
import java.io.File

val Project.requirements: Requirements
    get() = extensions.get(Requirements.Extension.key)!!

class Requirements(val descriptors: MutableList<Descriptor>, val files: MutableList<File>) : Hashable {
    object Extension : Project.Extension<Requirements> {
        override val key: Extendable.Key<Requirements> = Extendable.Key()

        override fun create(project: Project): Requirements {
            val config = object : ConfigurationView("requirements", project.config) {
                val file by string("file", default = "requirements.txt")
                val libraries by list<Map<String, String>>("libraries", default = emptyList())
            }

            val libraries = config.libraries.map { Descriptor(it["name"]!!, it["version"]!!) }.toMutableList()

            return Requirements(libraries, mutableListOf(File(project.workDir, config.file)))
        }
    }

    class Descriptor(val name: String, val version: String) : Hashable {
        override fun hash(): String {
            return listOf(name.hashable(), version.hashable()).hashable().hash()
        }
    }

    override fun hash(): String {
        return (files.map { it.hashable() } + descriptors).hashable().hash()
    }
}
