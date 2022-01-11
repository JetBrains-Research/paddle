package io.paddle.plugin.python.tasks.env

import io.paddle.plugin.python.extensions.repositories
import io.paddle.project.Project
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.Hashable
import io.paddle.utils.tasks.TaskDefaultGroups
import kotlin.system.measureTimeMillis

class ResolveRepositoriesTask(project: Project) : IncrementalTask(project) {
    override val id: String = "resolveRepositories"

    override val group: String = TaskDefaultGroups.BUILD

    override val inputs: List<Hashable> = listOf(project.repositories)
    // todo: outputs

    override fun act() {
        project.terminal.info("Resolving and indexing repositories...")
        val duration = measureTimeMillis { project.repositories.resolved }
        project.terminal.info("Finished: ${duration}ms")
    }
}
