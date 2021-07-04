package io.paddle.tasks

import io.paddle.project.Project
import io.paddle.terminal.TerminalUI

abstract class Task(val project: Project) {
    /**
     * Identifier of the task in the project.
     *
     * Note that this identifier will be used to call the task from the terminal.
     */
    abstract val id: String

    /**
     * Dependencies that should be called before this specific task.
     *
     * Note, that dependencies will be called via [run] method that may use
     * cached results
     */
    open val dependencies: List<Task> = emptyList()

    /**
     * Performs initial initialization during import of the whole Paddle project
     */
    abstract fun initialize()

    /**
     * Perform action which is the core essence of the task.
     *
     * Note, that in case Cacheable task [act] still will be called since it does not check for cache.
     */
    protected abstract fun act()

    /**
     * Run task with respect to the caches and current state.
     */
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
