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
            project.tasks.getOrFail("resolveInterpreter")
        ) + project.subprojects.getAllTasksById(this.id)

    override fun act() {
        val notResolved = project.requirements.descriptors
        val resolved = project.requirements.resolved
        val parsedDependencies = buildList {
            notResolved.forEach { descriptor ->
                val resolvedPackage = resolved.find { it.name == descriptor.name }
                val resolvePath = resolvedPackage?.repo?.let {
                    if (it != PyPackageRepository.PYPI_REPOSITORY) " @ ${resolvedPackage.distributionUrl} " else ""
                } ?: "" // unresolved package?
                descriptor.versionSpecifier?.clauses?.forEach { version ->
                    add("${descriptor.name}$resolvePath $version")
                } ?: add("${descriptor.name}$resolvePath")
            }
        }
        processDependencies(parsedDependencies)
    }

    private fun processDependencies(dependencies: List<String>) {
        val requirementsFile = File(project.workDir, REQUIREMENTS_FILE)
            .also { it.writeText("") }  // Clear file contents
        requirementsFile.writeText("")
        dependencies.forEach {
            requirementsFile.appendText(it)
            requirementsFile.appendText("\n")
        }
    }

    companion object {
        const val REQUIREMENTS_FILE = "requirements.txt"
    }
}
