package io.paddle.plugin.python.tasks.env

import io.paddle.plugin.python.extensions.requirements
import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.Hashable
import io.paddle.utils.tasks.TaskDefaultGroups
import kotlin.system.measureTimeMillis

class ResolveRequirementsTask(project: Project) : IncrementalTask(project) {
    override val id: String = "resolveRequirements"

    override val group: String = TaskDefaultGroups.BUILD

    override val inputs: List<Hashable> = listOf(project.requirements)
    // todo: outputs + caching resolved distributions?

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("resolveRepositories"))

    override fun act() {
        project.terminal.info("Resolving requirements...")
        val duration = measureTimeMillis { project.requirements.resolved }
        project.terminal.info("Finished: ${duration}ms")
    }
}
