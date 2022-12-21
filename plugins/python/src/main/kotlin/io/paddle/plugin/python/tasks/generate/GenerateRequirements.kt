package io.paddle.plugin.python.tasks.generate

import io.paddle.project.PaddleProject
import java.io.File

class GenerateRequirements(project: PaddleProject) : AbstractGenerateTask(project) {
    override val id: String = "requirements"

    override fun processDependencies(dependencies: List<String>) {
        val requirementsFile = File(project.workDir, REQUIREMENTS_FILE)
            .also { it.writeText("") }  // Clear file contents
        requirementsFile.writeText("")
        dependencies.forEach {
            requirementsFile.appendText(it)
            requirementsFile.appendText("\n")
        }
    }

    companion object {
        private const val REQUIREMENTS_FILE = "requirements.txt"
    }
}
