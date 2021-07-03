package io.paddle.tasks.incremental

import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.terminal.TerminalUI
import io.paddle.utils.Hashable
import io.paddle.utils.hashable

abstract class IncrementalTask(project: Project): Task(project) {
    open val inputs: List<Hashable> = emptyList()
    open val outputs: List<Hashable> = emptyList()

    private fun isUpToDate(): Boolean {
        return IncrementalCache.isUpToDate(id, inputs.hashable(), outputs.hashable()) && dependencies.all { it !is IncrementalTask || it.isUpToDate() }
    }

    override fun run() {
        if (isUpToDate()) {
            TerminalUI.echoln("> Task :${id}: ${TerminalUI.colored("UP-TO-DATE", TerminalUI.Color.GREEN)}")
            return
        }

        super.run()

        IncrementalCache.update(id, inputs.hashable(), outputs.hashable())
    }
}
