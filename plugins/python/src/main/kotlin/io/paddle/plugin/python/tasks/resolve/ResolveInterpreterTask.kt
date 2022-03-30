package io.paddle.plugin.python.tasks.resolve

import io.paddle.plugin.python.extensions.interpreter
import io.paddle.plugin.python.tasks.PythonPluginTaskGroups
import io.paddle.project.Project
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.Hashable
import kotlin.system.measureTimeMillis

class ResolveInterpreterTask(project: Project) : IncrementalTask(project) {
    override val id: String = "resolveInterpreter"

    override val group: String = PythonPluginTaskGroups.RESOLVE

    // Inputs: current configuration in the paddle.yaml file for interpreter
    override val inputs: List<Hashable> = listOf(project.interpreter)

    override fun act() {
        project.terminal.info("Resolving interpreter...")
        val duration = measureTimeMillis { project.interpreter.resolved }
        project.terminal.info("Finished: ${duration}ms")
    }
}
