package io.paddle.tasks

import io.paddle.plugin.standard.extensions.route
import io.paddle.project.Project
import io.paddle.terminal.CommandOutput
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
    open fun initialize() {
    }

    /**
     * Perform action which is the core essence of the task.
     *
     * Note, that in case Cacheable task [act] still will be called since it does not check for cache.
     * To check for the caches and current state, use [run].
     */
    protected abstract fun act()

    /**
     * Decorated version of [act]: prints current state to project's terminal.
     */
    protected open fun execute() {
        val taskRoute = ":" + project.route.joinToString(":") + ":$id"
        project.terminal.commands.stdout(CommandOutput.Command.Task(taskRoute, CommandOutput.Command.Task.Status.EXECUTE))

        try {
            act()
        } catch (e: ActException) {
            e.message?.let { project.terminal.error(it) }
            project.terminal.commands.stdout(CommandOutput.Command.Task(taskRoute, CommandOutput.Command.Task.Status.FAILED))
            throw e
        }

        project.terminal.commands.stdout(CommandOutput.Command.Task(taskRoute, CommandOutput.Command.Task.Status.DONE))
    }

    /**
     * Run task with respect to the caches and current state.
     */
    open fun run() {
        executionOrder.forEach { it.execute() }
    }

    open val executionOrder: ExecutionOrder
        get() = ExecutionOrder(this)

    class ExecutionOrder(root: Task) : Iterable<Task> {
        private val visited: MutableSet<Task>
        private val topologicalOrder: MutableList<Task>

        init {
            visited = HashSet()
            topologicalOrder = ArrayList()
            dfs(root)
        }

        override fun iterator() = topologicalOrder.iterator()

        private fun dfs(task: Task) {
            visited.add(task)
            for (dep in task.dependencies) {
                if (dep !in visited) {
                    dfs(dep)
                }
            }
            topologicalOrder.add(task)
        }
    }

    class ActException(reason: String) : Exception(reason)

    override fun hashCode(): Int = id.hashCode()
}
