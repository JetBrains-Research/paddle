package io.paddle.plugin.python.tasks.resolve

import io.paddle.plugin.python.extensions.requirements
import io.paddle.plugin.python.tasks.PythonPluginTaskGroups
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import java.io.File

class GenerateRequirements(project: PaddleProject) : IncrementalTask(project) {
    override val group: String = PythonPluginTaskGroups.RESOLVE
    override val id: String = "requirements"
    override val dependencies: List<Task>
        get() = listOf(
            project.tasks.getOrFail("resolveRepositories"),
            project.tasks.getOrFail("resolveInterpreter"),
        ) + project.subprojects.getAllTasksById(this.id)

    override fun act() {
        val requirementsFile = File(project.workDir, REQUIREMENTS_FILE)
            .also { it.writeText("") }  // Clear file contents
        val notResolved = project.requirements.descriptors
        notResolved.forEach { descriptor ->
            descriptor.versionSpecifier?.clauses?.forEach { version ->
                requirementsFile.appendText("${descriptor.name} $version # ${descriptor.type}\n")
            } ?: requirementsFile.appendText("${descriptor.name} # ${descriptor.type}\n")

        }
    }

    companion object {
        private const val REQUIREMENTS_FILE = "requirements.txt"
    }
}
