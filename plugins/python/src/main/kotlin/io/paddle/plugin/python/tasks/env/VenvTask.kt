package io.paddle.plugin.python.tasks.env

import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.standard.tasks.clean
import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.Hashable
import io.paddle.utils.tasks.TaskDefaultGroups
import kotlin.system.measureTimeMillis

class VenvTask(project: Project) : IncrementalTask(project) {
    override val id: String = "venv"

    override val group: String = TaskDefaultGroups.BUILD

    override val inputs: List<Hashable> = listOf(project.environment)
    override val outputs: List<Hashable> = listOf(project.environment)

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("resolveInterpreter"))

    override fun initialize() {
        project.requirements.descriptors.add(Requirements.Descriptor("wheel", "0.36.2", Repositories.Descriptor.PYPI.name))
        project.tasks.clean.locations.add(project.environment.venv)
    }

    override fun act() {
        project.terminal.info("Creating virtual environment...")
        val duration = measureTimeMillis {
            project.environment.initialize().orElse { throw ActException("Virtualenv creation has failed") }
        }
        project.terminal.info("Finished: ${duration}ms")
    }
}
