package io.paddle.tasks.incremental

import io.paddle.project.PaddleProject
import io.paddle.project.extensions.routeAsString
import io.paddle.tasks.Task
import io.paddle.terminal.CommandOutput
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable

/**
 * Task that uses caches to make sure that it will not be re-executed each time.
 *
 * Note, that to make use of incremental caching you should define [inputs] and [outputs]]
 */
abstract class IncrementalTask(project: PaddleProject) : Task(project) {
    /** Input of the task that should be used during incrementallity check */
    open val inputs: List<Hashable> = emptyList()

    /** Output of the task that should be used during incrementallity check */
    open val outputs: List<Hashable> = emptyList()

    protected fun isUpToDate(extraArgs: Map<String, String>): Boolean {
        if (inputs.isEmpty() && outputs.isEmpty()) {
            return false
        }
        val inputsWithArgs: List<Hashable> = inputs + extraArgs.toList().map { it.hashable()  }
        return IncrementalCache(project).isUpToDate(id, inputsWithArgs.hashable(), outputs.hashable())
    }

    override fun execute(extraArgs: Map<String, String>) {
        if (isUpToDate(extraArgs)) {
            val taskRoute = project.routeAsString + ":$id"
            project.terminal.commands.stdout(CommandOutput.Command.Task(taskRoute, CommandOutput.Command.Task.Status.UP_TO_DATE))
            return
        }

        super.execute(extraArgs)

        IncrementalCache(project).update(id, inputs.hashable(), outputs.hashable())
    }

    private fun Pair<String, String>.hashable() = listOf(first.hashable(), second.hashable()).hashable()
}
