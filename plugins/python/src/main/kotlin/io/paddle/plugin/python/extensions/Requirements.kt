package io.paddle.plugin.python.extensions

import io.paddle.project.Project
import io.paddle.utils.Hashable
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hashable

val Project.requirements: Requirements
    get() = extensions.get(Requirements.Extension.key)!!

class Requirements(val descriptors: MutableList<Descriptor>) : Hashable {
    object Extension : Project.Extension<Requirements> {
        override val key: Extendable.Key<Requirements> = Extendable.Key()

        override fun create(project: Project): Requirements {
            val config = object : ConfigurationView("requirements", project.config) {
                val libraries by list<Map<String, String>>("libraries", default = emptyList())
            }
            val descriptors = config.libraries.map { Descriptor(it["name"]!!, it["version"]!!) }.toMutableList()

            return Requirements(descriptors)
        }
    }

    data class Descriptor(val name: String, val version: String) : Hashable {
        val distInfoDirName = "${name}-${version}.dist-info"

        override fun hash(): String {
            return listOf(name.hashable(), version.hashable()).hashable().hash()
        }
    }

    override fun hash(): String {
        return descriptors.hashable().hash()
    }
}
