package io.paddle.plugin.python.tasks.env

import io.paddle.plugin.python.dependencies.GlobalCacheRepository
import io.paddle.plugin.python.extensions.*
import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import io.paddle.utils.tasks.TaskDefaultGroups
import kotlin.system.measureTimeMillis

class InstallTask(project: Project) : IncrementalTask(project) {
    override val id: String = "install"

    override val group: String = TaskDefaultGroups.BUILD

    override val inputs: List<Hashable> = listOf(project.environment, project.repositories, project.requirements)
    override val outputs: List<Hashable> = listOf(project.environment.venv.hashable())

    override val dependencies: List<Task>
        get() = listOf(
            project.tasks.getOrFail("venv"),
            project.tasks.getOrFail("resolveRequirements"),
        )

    override fun act() {
        project.terminal.info("Installing requirements...")
        val duration = measureTimeMillis {
            GlobalCacheRepository.updateCache()
            for (pkg in project.requirements.resolved) {
                project.environment.install(pkg)
            }
        }
        project.terminal.info("Finished: ${duration}ms")
    }
}
