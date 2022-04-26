package io.paddle.plugin.python.tasks.venv

import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.tasks.PythonPluginTaskGroups
import io.paddle.plugin.standard.extensions.subprojects
import io.paddle.plugin.standard.tasks.clean
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.lightHashable
import kotlin.system.measureTimeMillis

class VenvTask(project: PaddleProject) : IncrementalTask(project) {
    override val id: String = "venv"

    override val group: String = PythonPluginTaskGroups.VENV

    override val inputs: List<Hashable>
        get() = listOf(project.interpreter)
    override val outputs: List<Hashable>
        get() = listOf(project.environment.venv.lightHashable())

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("resolveInterpreter")) + project.subprojects.getAllTasksById(this.id)

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
