package io.paddle.plugin.python.tasks.generate

import io.paddle.plugin.python.extensions.repositories
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
        // TODO: add support of find-links
        val notResolved = project.requirements.descriptors
        val resolved = project.requirements.resolved
        val requirementsFile = File(REQUIREMENTS_FILE).also { it.writeText("") }

        // FIXME: local/packages with direct link are not printed correctly
        notResolved.groupBy { descriptor -> resolved.find { it.name == descriptor.name }?.repo }.forEach { (repo, pkgs) ->
            if (repo != null && repo != project.repositories.resolved.primarySource) {
                requirementsFile.appendText("--extra-index-url ${repo.url}\n")
            }
            pkgs.forEach {
                requirementsFile.appendText("${it.name}${it.versionSpecifier?.clauses?.joinToString(separator = ", ", prefix = " ") ?: ""}\n")
            }
        }
    }

    companion object {
        const val REQUIREMENTS_FILE = "requirements.txt"
    }
}
