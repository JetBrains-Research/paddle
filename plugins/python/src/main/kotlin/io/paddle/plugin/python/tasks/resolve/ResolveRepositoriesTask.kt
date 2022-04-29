package io.paddle.plugin.python.tasks.resolve

import io.paddle.plugin.python.extensions.repositories
import io.paddle.plugin.python.tasks.PythonPluginTaskGroups
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import kotlin.system.measureTimeMillis

class ResolveRepositoriesTask(project: PaddleProject) : IncrementalTask(project) {
    override val id: String = "resolveRepositories"

    override val group: String = PythonPluginTaskGroups.RESOLVE

    override val dependencies: List<Task>
        get() = project.subprojects.getAllTasksById(this.id)

    override fun act() {
        project.terminal.info("Resolving and indexing repositories...")
        val duration = measureTimeMillis { project.repositories.resolved }
        project.terminal.info("Finished: ${duration}ms")
    }
}
