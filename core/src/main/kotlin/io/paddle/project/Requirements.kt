package io.paddle.project

import io.paddle.utils.Hashable
import io.paddle.utils.config.Configuration
import io.paddle.utils.hashable
import java.io.File

class Requirements(val descriptors: List<Descriptor>, val files: List<File>) : Hashable {
    companion object {
        fun from(configuration: Configuration): Requirements {
            val files = listOf(File(configuration.get<String>("environment.requirements") ?: "requirements.txt"))

            val additional = listOf(
                Descriptor("wheel", "0.36.2"),
                Descriptor("pylint", configuration.get<String>("tasks.linter.pylint.version") ?: "2.8.3"),
                Descriptor("mypy", configuration.get<String>("tasks.linter.mypy.version") ?: "0.902"),
                Descriptor("pytest", configuration.get<String>("tasks.tests.pytest.version") ?: "6.2.4")
            )

            return Requirements(additional, files)
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
