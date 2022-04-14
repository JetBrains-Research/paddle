package io.paddle.plugin.python.tasks.install

import io.paddle.plugin.python.dependencies.GlobalCacheRepository
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.tasks.PythonPluginTaskGroups
import io.paddle.plugin.standard.extensions.subprojects
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import kotlin.system.measureTimeMillis

class InstallTask(project: PaddleProject) : IncrementalTask(project) {
    override val id: String = "install"

    override val group: String = PythonPluginTaskGroups.INSTALL

    override val inputs: List<Hashable> = listOf(project.repositories, project.requirements)
    override val outputs: List<Hashable> = listOf(project.environment.venv.hashable())

    override val dependencies: List<Task>
        get() = listOf(
            project.tasks.getOrFail("venv"),
            project.tasks.getOrFail("resolveRequirements"),
        ) + project.subprojects.getAllTasksById(this.id)

    override fun act() {
        project.terminal.info("Installing requirements...")
        val duration = measureTimeMillis {
            GlobalCacheRepository.updateCache()
            for (pkg in project.requirements.resolved) {
                project.environment.install(pkg)
            }
            for (pkg in project.subprojects.flatMap { it.requirements.resolved }) {
                project.environment.install(pkg)
            }
        }
        project.terminal.info("Finished: ${duration}ms")
    }
}