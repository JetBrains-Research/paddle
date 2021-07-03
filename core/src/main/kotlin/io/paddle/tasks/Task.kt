package io.paddle.tasks

import io.paddle.project.Project
import io.paddle.terminal.TerminalUI

abstract class Task(val project: Project) {
    abstract val id: String

    open val dependencies: List<Task> = emptyList()

    abstract fun act()

    open fun run() {
        for (dep in dependencies) {
            dep.run()
        }

        TerminalUI.echoln("> Task :${id}: ${TerminalUI.colored("EXECUTE", TerminalUI.Color.YELLOW)}")

        try {
            act()
        } catch (e: ActException) {
            TerminalUI.echoln("> Task :${id}: ${TerminalUI.colored("FAILED", TerminalUI.Color.RED)}")
            return
        }

        TerminalUI.echoln("> Task :${id}: ${TerminalUI.colored("DONE", TerminalUI.Color.GREEN)}")
    }

    class ActException(reason: String) : Exception(reason)
}
