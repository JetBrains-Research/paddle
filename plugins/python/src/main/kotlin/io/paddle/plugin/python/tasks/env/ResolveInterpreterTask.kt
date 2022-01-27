package io.paddle.plugin.python.tasks.env

import io.paddle.plugin.python.dependencies.index.PyInterpreter
import io.paddle.plugin.python.extensions.environment
import io.paddle.project.Project
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.Hashable
import io.paddle.utils.hashable
import io.paddle.utils.tasks.TaskDefaultGroups
import kotlin.system.measureTimeMillis

class ResolveInterpreterTask(project: Project) : IncrementalTask(project) {
    override val id: String = "resolveInterpreter"

    override val group: String = TaskDefaultGroups.BUILD

    // Inputs: current configuration in the paddle.yaml file for venv
    override val inputs: List<Hashable> = listOf(project.environment)

    // Outputs: checksum for the directory with the chosen interpreter
    override val outputs: List<Hashable> = listOf(PyInterpreter.getLocation(project.environment.pythonVersion, project).toFile().hashable())

    override fun act() {
        project.terminal.info("Resolving interpreter...")
        val duration = measureTimeMillis { project.environment.interpreter }
        project.terminal.info("Finished: ${duration}ms")
    }
}
