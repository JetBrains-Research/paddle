package io.paddle.plugin.python.dependencies.setup

import io.paddle.plugin.python.extensions.buildEnvironment
import io.paddle.plugin.python.extensions.interpreter
import io.paddle.plugin.python.utils.camelToSnakeCase
import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.PaddleProject
import io.paddle.project.extensions.descriptor
import java.io.File

data class SetupConfig(val project: PaddleProject) {
    data class Metadata(
        val name: String,
        val version: String,
        val author: String,
        val authorEmail: String?,
        val description: String?,
        val longDescription: String?,
        val longDescriptionContentType: String?,
        val url: String?,
        val classifiers: List<String>?
    ) : Toml

    data class Options(
        val packageDir: Map<String, String>,
        val packages: List<String>,
        val pythonRequires: String
    ) : Toml

    interface Toml {
        fun dump(): List<String> {
            val data = arrayListOf("[${this.javaClass.simpleName.lowercase()}]")
            for (field in this.javaClass.declaredFields) {
                field.trySetAccessible()
                val key = field.name.camelToSnakeCase()
                when (val value = field.get(this)) {
                    is String -> {
                        data += "$key = $value"
                    }

                    is List<*> -> {
                        data += "$key = "
                        value.forEach { data += "\t$it" }
                    }

                    is Map<*, *> -> {
                        data += "$key = "
                        value.entries.forEach { data += "\t${it.key} = ${it.value}" }
                    }
                }
            }
            return data
        }
    }

    val metadata: Metadata
        get() = Metadata(
            name = project.descriptor.name,
            version = project.descriptor.version,
            author = project.descriptor.author ?: "Unknown Author".also {
                project.terminal.warn("Author is not provided. Default value will be set: Unknown Author")
            },
            authorEmail = project.descriptor.authorEmail ?: (null).also {
                project.terminal.warn("Author's email is not provided.")
            },
            description = project.descriptor.description ?: (null).also {
                project.terminal.warn("Description is not provided.")
            },
            longDescription = project.buildEnvironment.readme?.name?.let { "file: $it" } ?: (null).also {
                project.terminal.warn("Long description (README or README.md file in the workDir) is not provided.")
            },
            longDescriptionContentType = project.buildEnvironment.readme?.run { "text/markdown" },
            url = project.descriptor.url ?: (null).also {
                project.terminal.warn("Url is not provided.")
            },
            classifiers = project.descriptor.classifiers ?: (null).also {
                project.terminal.warn("Classifiers are not provided.")
            }
        )

    val options: Options
        get() = Options(
            packageDir = mapOf("" to project.roots.sources.relativeTo(project.workDir).path),
            packages = listOf("find:"),
            pythonRequires = ">=${project.interpreter.pythonVersion.number}"
        )

    fun create(file: File) {
        val lines = metadata.dump() + "\n" +
            options.dump() + "\n" +
            listOf(
                "[options.packages.find]",
                "where = ${project.roots.sources.relativeTo(project.workDir).path}"
            )
        file.writeText(lines.joinToString("\n"))
    }
}
