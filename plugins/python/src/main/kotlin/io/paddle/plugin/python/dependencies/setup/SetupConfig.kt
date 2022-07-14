package io.paddle.plugin.python.dependencies.setup

import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.hasPython
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
        val keywords: String?,
        val classifiers: List<String>?
    ) : Toml

    data class Options(
        val packageDir: Map<String, String>,
        val packages: List<String>,
        val installRequires: List<String>?
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
            version = project.metadata.version,
            author = project.metadata.author,
            authorEmail = project.metadata.authorEmail,
            description = project.metadata.description,
            longDescription = project.buildEnvironment.readme?.name?.let { "file: $it" } ?: (null).also {
                project.terminal.warn("Long description (README or README.md file in the ${project.workDir.absolutePath}) is not provided.")
            },
            longDescriptionContentType = project.buildEnvironment.readme?.run { "text/markdown" },
            url = project.metadata.url,
            keywords = project.metadata.keywords,
            classifiers = project.metadata.classifiers
        )

    val options: Options
        get() = Options(
            packageDir = mapOf("" to project.roots.sources.relativeTo(project.workDir).path),
            packages = listOf("find:"),
            installRequires = project.requirements.descriptors                             // user-defined requirements
                .filter { it.type == Requirements.Descriptor.Type.MAIN }
                .map { it.toString() }
                + project.subprojects.filter { it.hasPython }.map { it.descriptor.name }   // subproject dependencies which also should be published
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
