package io.paddle.plugin.python.tasks.env

import io.paddle.plugin.python.extensions.*
import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.Hashable
import io.paddle.utils.tasks.TaskDefaultGroups

class InstallTask(project: Project) : IncrementalTask(project) {
    override val id: String = "install"

    override val group: String = TaskDefaultGroups.BUILD

    override val inputs: List<Hashable> = listOf(project.environment, project.repositories, project.requirements)
    override val outputs: List<Hashable> = listOf(project.environment, project.repositories, project.requirements)

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("venv"))

    override fun initialize() {
    }

    override fun act() {
        for (pkg in project.requirements.resolved) {
            project.environment.install(pkg)
        }
    }
}
