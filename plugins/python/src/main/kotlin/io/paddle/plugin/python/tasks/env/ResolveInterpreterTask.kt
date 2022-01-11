package io.paddle.plugin.python.tasks.env

import io.paddle.plugin.python.PaddlePyConfig
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

    override val inputs: List<Hashable> = listOf(project.environment)
    override val outputs: List<Hashable> = listOf(PaddlePyConfig.interpreters.resolve(project.environment.pythonVersion.number).toFile().hashable())

    override fun act() {
        project.terminal.info("Resolving interpreter...")
        val duration = measureTimeMillis { project.environment.interpreter }
        project.terminal.info("Finished: ${duration}ms")
    }
}
