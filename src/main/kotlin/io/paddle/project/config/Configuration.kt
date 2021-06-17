package io.paddle.project.config

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
class Configuration(
    val descriptor: Descriptor,
    val environment: Environment = Environment(),
    val roots: Roots = Roots(),
    val tasks: Tasks = Tasks()
) {
    companion object {
        fun from(file: File): Configuration {
            return Yaml.default.decodeFromString(serializer(), file.readText())
        }
    }

    @Serializable
    data class Descriptor(
        val group: String,
        val version: String
    )

    @Serializable
    data class Roots(
        val sources: List<String> = listOf("src/main"),
        val tests: List<String> = listOf("src/test")
    )

    @Serializable
    data class Environment(
        val virtualenv: String = ".venv",
        val requirements: String = "requirements.txt"
    )

    @Serializable
    data class Tasks(
        val linter: Linter = Linter(),
        val tests: Tests = Tests(),
        val execution: List<Execution> = emptyList()
    ) {
        @Serializable
        data class Linter(
            val pylint: PyLintTask = PyLintTask(),
            val mypy: MyPyTask = MyPyTask()
        ) {
            @Serializable
            class PyLintTask(val enabled: Boolean = true, val version: String = "2.8.3")

            @Serializable
            class MyPyTask(val enabled: Boolean = true, val version: String = "0.902")
        }

        @Serializable
        data class Tests(val pytest: PyTestTask = PyTestTask()) {
            @Serializable
            class PyTestTask(val enabled: Boolean = true, val version: String = "6.2.4")
        }

        @Serializable
        data class Execution(val entrypoint: String, val id: String)
    }
}
