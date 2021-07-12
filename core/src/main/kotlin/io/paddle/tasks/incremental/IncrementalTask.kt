package io.paddle.tasks.incremental

import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.terminal.TerminalUI
import io.paddle.utils.Hashable
import io.paddle.utils.hashable

/**
 * Task that uses caches to make sure that it will not be re-executed each time.
 *
 * Note, that to make use of incremental caching you should define [inputs] and [outputs]]
 */
abstract class IncrementalTask(project: Project) : Task(project) {
    /** Input of the task that should be used during incrementallity check */
    open val inputs: List<Hashable> = emptyList()

    /** Output of the task that should be used during incrementallity check */
    open val outputs: List<Hashable> = emptyList()

    private fun isUpToDate(): Boolean {
        return IncrementalCache(project).isUpToDate(id, inputs.hashable(), outputs.hashable()) && dependencies.all { it is IncrementalTask && it.isUpToDate() }
    }

    override fun run() {
        if (isUpToDate()) {
            project.terminal.echoln("> Task :${id}: ${project.terminal.colored("UP-TO-DATE", TerminalUI.Color.GREEN)}")
            return
        }

        super.run()

        IncrementalCache(project).update(id, inputs.hashable(), outputs.hashable())
    }
}
