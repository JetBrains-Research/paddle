package io.paddle.plugin.python.tasks.generate

import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.extensions.requirements
import io.paddle.plugin.python.tasks.PythonPluginTaskGroups
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import java.io.File

class GenerateRequirements(project: PaddleProject) : IncrementalTask(project) {
    override val group: String = PythonPluginTaskGroups.GENERATE
    override val id: String = "requirements"
    override val dependencies: List<Task>
        get() = listOf(
            project.tasks.getOrFail("resolveRepositories"),
            project.tasks.getOrFail("resolveInterpreter"),
            project.tasks.getOrFail("resolveInterpreter")
        ) + project.subprojects.getAllTasksById(this.id)

    override fun act() {
        val notResolved = project.requirements.descriptors
        val resolved = project.requirements.resolved
        val requirementsFile = File(project.workDir, REQUIREMENTS_FILE)
            .also { it.writeText("") }  // Clear file contents

        notResolved.forEach { descriptor ->
            val resolvedPackage = resolved.find { it.name == descriptor.name }
            val resolvePath = resolvedPackage?.repo?.let {
                if (it != PyPackageRepository.PYPI_REPOSITORY) " @ ${resolvedPackage.distributionUrl} " else ""
            } ?: "" // unresolved package?
            descriptor.versionSpecifier?.clauses?.forEach { version ->
                requirementsFile.appendText("${descriptor.name}$resolvePath $version # ${descriptor.type}\n")
            } ?: requirementsFile.appendText("${descriptor.name}$resolvePath # ${descriptor.type}\n")
        }
    }

    companion object {
        private const val REQUIREMENTS_FILE = "requirements.txt"
    }
}
