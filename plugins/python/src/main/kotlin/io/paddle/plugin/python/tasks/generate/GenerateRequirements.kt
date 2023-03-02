package io.paddle.plugin.python.tasks.generate

import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.extensions.repositories
import io.paddle.plugin.python.extensions.requirements
import io.paddle.plugin.python.tasks.PythonPluginTaskGroups
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import java.io.File

class GenerateRequirements(project: PaddleProject) : IncrementalTask(project) {
    override val group: String = PythonPluginTaskGroups.GENERATE
    override val id: String = "requirements"
    override val inputs: List<Hashable>
        get() = listOf(project.requirements.descriptors.hashable(), project.requirements.resolved.map { it.toString().hashable() }.toList().hashable(),
            project.repositories.descriptors.hashable())
    override val outputs: List<Hashable>
        get() = listOf(getRequirementsText().hashable())
    override val dependencies: List<Task>
        get() = listOf(
            project.tasks.getOrFail("resolveRepositories"),
            project.tasks.getOrFail("resolveInterpreter"),
            project.tasks.getOrFail("resolveRequirements")
        ) + project.subprojects.getAllTasksById(this.id)

    private fun getRequirementsText(): String {
        val notResolved = project.requirements.descriptors
        val resolved = project.requirements.resolved

        // FIXME: local/packages with direct link are not printed correctly
        return buildString {
            if (project.repositories.resolved.primarySource != PyPackageRepository.PYPI_REPOSITORY) {
                appendLine("--index-url ${project.repositories.resolved.primarySource.urlSimple}")
            }
            project.repositories.resolved.linkSources.forEach { appendLine("--find-links $it") }
            notResolved.groupBy { descriptor -> resolved.find { it.name == descriptor.name }?.repo }.forEach { (repo, pkgs) ->
                if (repo != null && repo != project.repositories.resolved.primarySource) {
                    appendLine("--extra-index-url ${repo.urlSimple}")
                }
                pkgs.forEach {
                    appendLine("${it.name}${it.versionSpecifier?.clauses?.joinToString(separator = ", ", prefix = " ") ?: ""}")
                }
            }
        }
    }

    override fun act() {
        File(project.workDir, REQUIREMENTS_FILE).run {
            if (exists() && this.readText().isNotEmpty()) {
                throw ActException("The requirements.txt file in ${project.workDir.absolutePath} is exists. Please clear the file, or delete it.")
            }
            writeText(getRequirementsText())
        }

    }

    companion object {
        const val REQUIREMENTS_FILE = "requirements.txt"
    }
}
