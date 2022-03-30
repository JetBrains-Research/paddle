package io.paddle.plugin.python.tasks.resolve

import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.tasks.PythonPluginTaskGroups
import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import kotlin.system.measureTimeMillis

class ResolveRequirementsTask(project: Project) : IncrementalTask(project) {
    override val id: String = "resolveRequirements"

    override val group: String = PythonPluginTaskGroups.RESOLVE

    override val inputs: List<Hashable> = listOf(project.requirements, project.repositories, project.interpreter)
    override val outputs: List<Hashable> =
        if (project.environment.venv.exists())
            project.environment.venv.pyPackageFiles.map { it.hashable() }
        else
            emptyList()

    override val dependencies: List<Task>
        get() = listOf(
            project.tasks.getOrFail("resolveRepositories"),
            project.tasks.getOrFail("resolveInterpreter")
        )

    override fun act() {
        project.terminal.info("Resolving requirements...")
        val duration = measureTimeMillis { project.requirements.resolved }
        project.terminal.info("Finished: ${duration}ms")
    }
}
