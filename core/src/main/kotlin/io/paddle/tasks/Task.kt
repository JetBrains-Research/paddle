package io.paddle.tasks

import io.paddle.project.Project
import io.paddle.terminal.TerminalUI
import io.paddle.utils.tasks.TaskDefaultGroups

abstract class Task(val project: Project) {
    /**
     * Identifier of the task in the project.
     *
     * Note that this identifier will be used to call the task from the terminal.
     */
    abstract val id: String

    /**
     * Tasks are grouped into categories by semantics.
     *
     * Use [TaskDefaultGroups] for standard groups for tasks
     */
    abstract val group: String

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

        project.terminal.echoln("> Task :${id}: ${project.terminal.colored("EXECUTE", TerminalUI.Color.YELLOW)}")

        try {
            act()
        } catch (e: ActException) {
            project.terminal.echoln("> Task :${id}: ${project.terminal.colored("FAILED", TerminalUI.Color.RED)}")
            return
        }

        project.terminal.echoln("> Task :${id}: ${project.terminal.colored("DONE", TerminalUI.Color.GREEN)}")
    }

    class ActException(reason: String) : Exception(reason)
}
