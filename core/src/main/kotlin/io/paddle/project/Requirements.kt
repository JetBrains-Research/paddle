package io.paddle.project

import io.paddle.utils.Hashable
import io.paddle.utils.config.Configuration
import io.paddle.utils.hashable
import java.io.File

class Requirements(val descriptors: MutableList<Descriptor>, val files: MutableList<File>) : Hashable {
    companion object {
        fun from(configuration: Configuration): Requirements {
            val files = listOf(File(configuration.get<String>("environment.requirements") ?: "requirements.txt"))

            return Requirements(ArrayList(), files.toMutableList())
        }
    }

    class Descriptor(val id: String, val version: String) : Hashable {
        override fun hash(): String {
            return listOf(id.hashable(), version.hashable()).hashable().hash()
        }
    }

    override fun hash(): String {
        return (files.map { it.hashable() } + descriptors).hashable().hash()
    }
}
