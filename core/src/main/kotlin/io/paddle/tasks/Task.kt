package io.paddle.tasks

import io.paddle.plugin.standard.extensions.registry
import io.paddle.project.PaddleProject
import io.paddle.project.extensions.routeAsString
import io.paddle.terminal.CommandOutput
import io.paddle.utils.tasks.TaskDefaultGroups
import kotlinx.coroutines.*

abstract class Task(val project: PaddleProject) {
    /**
     * Identifier of the task in the project.
     *
     * Note that this identifier will be used to call the task from the terminal.
     */
    abstract val id: String

    val taskRoute: String
        get() = project.routeAsString + ":$id"

    /**
     * Short description of the task.
     */
    val description: String = ""

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
     * [act] with cli arguments support
     */
    protected open fun act(cliArgs: Map<String, String>) = act()

    /**
     * Decorated version of [act]: prints current state to project's terminal.
     */
    protected open fun execute(cliArgs: Map<String, String> = emptyMap()) {
        project.terminal.commands.stdout(
            CommandOutput.Command.Task(taskRoute, CommandOutput.Command.Task.Status.EXECUTE)
        )

        try {
            act(cliArgs)
        } catch (e: PaddleTaskCancellationException) {
            project.terminal.commands.stdout(
                CommandOutput.Command.Task(taskRoute, CommandOutput.Command.Task.Status.CANCELLED)
            )
            throw e
        } catch (e: Throwable) {
            e.message?.let { project.terminal.error(it) }
            if (project.registry.showStackTrace) {
                project.terminal.error(e.stackTraceToString())
            }
            project.terminal.commands.stdout(
                CommandOutput.Command.Task(taskRoute, CommandOutput.Command.Task.Status.FAILED)
            )
            throw e
        }

        project.terminal.commands.stdout(CommandOutput.Command.Task(taskRoute, CommandOutput.Command.Task.Status.DONE))
    }



    /**
     * Runs a task as a coroutine and creates another polling coroutine to perform graceful cancellation
     * (by killing all the created external processes).
     */
    open fun run(cancellationToken: CancellationToken = CancellationToken.None, cliArgs: Map<String, String> = emptyMap()) = runBlocking(Dispatchers.IO) {
        val job = launch {
            try {
                executionOrder.forEach {
                    it.execute(cliArgs)
                    yield()
                }
            } catch (e: CancellationException) {
                project.terminal.info("$taskRoute execution was cancelled.")
                throw PaddleTaskCancellationException()
            }
        }

        launch {
            while (job.isActive) {
                if (cancellationToken.isCancelled) {
                    for (process in project.executor.runningProcesses) {
                        process.destroy()
                        project.terminal.info("Cancelling process ${process.pid()}...}")
                    }
                    job.cancel()
                    break
                }
                delay(CancellationToken.STATE_CHECK_TIMEOUT_MS)
            }
        }
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

class PaddleTaskCancellationException : Exception()
