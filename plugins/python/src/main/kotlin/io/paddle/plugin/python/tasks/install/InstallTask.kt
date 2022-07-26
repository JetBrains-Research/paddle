package io.paddle.plugin.python.tasks.install

import io.paddle.plugin.python.dependencies.GlobalCacheRepository
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.tasks.PythonPluginTaskGroups
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.lightHashable
import kotlin.system.measureTimeMillis

class InstallTask(project: PaddleProject) : IncrementalTask(project) {
    override val id: String = "install"

    override val group: String = PythonPluginTaskGroups.INSTALL

    override val inputs: List<Hashable>
        get() = listOf(project.repositories, project.requirements)
    override val outputs: List<Hashable>
        get() = listOf(project.environment.venv.lightHashable())

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("resolveRequirements")) + project.subprojects.getAllTasksById(this.id)

    override fun act() {
        project.terminal.info("Installing requirements...")
        val duration = measureTimeMillis {
            GlobalCacheRepository.updateCache()
            project.requirements.resolved
                .minus(project.environment.venv.pyPackages)
                .forEach { project.environment.install(it) }
        }
        project.terminal.info("Finished: ${duration}ms")
    }
}
