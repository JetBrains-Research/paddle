package io.paddle.idea.execution.cmd

import com.intellij.util.execution.ParametersListUtil

/**
 * Same as [org.jetbrains.plugins.gradle.util.GradleCommandLine], but ScriptParameters are omitted (not supported by Paddle yet).
 */
class PaddleCommandLine(val tasksAndArguments: TasksAndArguments) {
    override fun toString() = tasksAndArguments.toString()

    data class Task(val name: String, val arguments: List<String>) {
        fun toList() = listOf(name) + arguments
        override fun toString() = toList().joinToString(" ")
    }

    data class TasksAndArguments(val tasks: List<Task>) {
        fun toList() = tasks.flatMap(Task::toList)
        override fun toString() = toList().joinToString(" ")
    }

    companion object {
        @JvmStatic
        fun parse(commandLine: String) = parse(ParametersListUtil.parse(commandLine, true, true))

        @JvmStatic
        fun parse(commandLine: List<String>): PaddleCommandLine {
            val state = ParserState(commandLine).apply {
                while (iterator.hasNext()) {
                    val token = iterator.next()
                    parseTask(token)
                }
            }
            return state.getParsedCommandLine()
        }

        private fun ParserState.parseTask(token: String) {
            tasks.add(Task(token, emptyList()))
        }

        private class ParserState(commandLine: List<String>) {
            val iterator = commandLine.iterator()

            val tasks = ArrayList<Task>()

            fun getParsedCommandLine(): PaddleCommandLine {
                return PaddleCommandLine(TasksAndArguments(tasks))
            }

        }
    }
}
