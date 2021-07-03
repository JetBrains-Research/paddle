package io.paddle.project

import io.paddle.project.config.Configuration
import io.paddle.utils.Hashable
import io.paddle.utils.hashable
import java.io.File

class Requirements(val descriptors: List<Descriptor>, val files: List<File>) : Hashable {
    companion object {
        fun from(configuration: Configuration): Requirements {
            val files = listOf(File(configuration.environment.requirements))

            val additional = listOf(
                Descriptor("wheel", "0.36.2"),
                Descriptor("pylint", configuration.tasks.linter.pylint.version),
                Descriptor("mypy", configuration.tasks.linter.mypy.version),
                Descriptor("pytest", configuration.tasks.tests.pytest.version)
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
