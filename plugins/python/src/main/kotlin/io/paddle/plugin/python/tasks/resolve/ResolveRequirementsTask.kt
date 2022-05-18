package io.paddle.plugin.python.tasks.resolve

import io.paddle.plugin.python.extensions.requirements
import io.paddle.plugin.python.tasks.PythonPluginTaskGroups
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import kotlin.system.measureTimeMillis

class ResolveRequirementsTask(project: PaddleProject) : IncrementalTask(project) {
    override val id: String = "resolveRequirements"

    override val group: String = PythonPluginTaskGroups.RESOLVE

    override val dependencies: List<Task>
        get() = listOf(
            project.tasks.getOrFail("resolveRepositories"),
            project.tasks.getOrFail("resolveInterpreter"),
            project.tasks.getOrFail("venv"),
        ) + project.subprojects.getAllTasksById(this.id)

    override fun act() {
        project.terminal.info("Resolving requirements...")
        val duration = measureTimeMillis { project.requirements.resolved }
        project.terminal.info("Finished: ${duration}ms")
    }
}
