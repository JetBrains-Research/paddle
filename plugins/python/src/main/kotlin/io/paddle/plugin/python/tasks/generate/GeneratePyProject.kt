package io.paddle.plugin.python.tasks.generate

import com.akuleshov7.ktoml.Toml
import io.paddle.plugin.python.extensions.metadata
import io.paddle.project.PaddleProject
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.io.File

class GeneratePyProject(project: PaddleProject) : AbstractGenerateTask(project) {
    override val id: String = "pyProject"

    override fun processDependencies(dependencies: List<String>) {
        val pyProjectFile = File(project.workDir, PYPROJECT_FILE)
            .also { it.writeText("") }  // Clear file contents
        pyProjectFile.writeText(
            Toml.encodeToString(
                PyProjectTOML(
                    project.id,
                    project.metadata.version,
                    project.metadata.description,
                    dependencies
                )
            )
        )
    }

    @Serializable
    private data class PyProjectTOML(
        val name: String,
        val version: String,
        val description: String,
//        val author: String, // KToml doesn't support inline tables
        val dependencies: List<String>
    )

    companion object {
        const val PYPROJECT_FILE = "pyproject.toml"
    }
}
