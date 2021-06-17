package io.paddle.schema

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
class PaddleSchema(
    val descriptor: Descriptor,
    val environment: Environment = Environment(),
    val roots: Roots = Roots(),
    val tasks: Tasks = Tasks()
) {
    companion object {
        fun from(file: File): PaddleSchema {
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
        data class Linter(val pylint: Task = Task(enabled = true), val mypy: Task = Task(enabled = true))

        @Serializable
        data class Tests(val pytest: Task = Task(enabled = true))

        @Serializable
        data class Task(val enabled: Boolean)

        @Serializable
        data class Execution(val entrypoint: String, val id: String)
    }
}