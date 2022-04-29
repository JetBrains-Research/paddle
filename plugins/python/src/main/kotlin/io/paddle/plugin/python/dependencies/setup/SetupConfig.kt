package io.paddle.plugin.python.dependencies.setup

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
        val authorEmail: String,
        val description: String,
        val longDescription: String,
        val longDescriptionContentType: String,
        val url: String?,
        val classifiers: List<String>
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

    val metadata = Metadata(
        name = project.descriptor.name,
        version = project.descriptor.version,
        author = project.descriptor.author ?: "Unknown Author",
        authorEmail = project.descriptor.authorEmail ?: "unknown@example.com",
        description = project.descriptor.description ?: "The author did not provide any description",
        longDescription = "file: README.md",
        longDescriptionContentType = "text/markdown",
        url = project.descriptor.url,
        classifiers = listOf("Programming Language :: Python :: ${project.interpreter.pythonVersion.major}")
    )

    val options = Options(
        packageDir = mapOf("" to project.roots.sources.first().relativeTo(project.workDir).path), // FIXME: add other roots?
        packages = listOf("find:"),
        pythonRequires = ">=${project.interpreter.pythonVersion.number}"
    )

    fun create(file: File) {
        val lines = metadata.dump() + "\n" +
            options.dump() + "\n" +
            listOf(
                "[options.packages.find]",
                "where = ${project.roots.sources.first().relativeTo(project.workDir).path}"
            )
        file.writeText(lines.joinToString("\n"))
    }
}
