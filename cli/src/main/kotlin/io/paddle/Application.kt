package io.paddle

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import io.paddle.plugin.standard.extensions.plugins
import io.paddle.plugin.standard.extensions.subprojects
import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.terminal.Terminal
import io.paddle.terminal.TextOutput
import java.io.File

class Paddle(private val project: Project) : CliktCommand() {
    private val taskRoute by argument(
        "task",
        "Use full name of the task to run it (e.g., ':subproject:runTask') " +
            "OR short name to run the task for global parent project (e.g., 'clean')"
    )

    override fun run() {
        try {
            if (taskRoute.startsWith(':')) {
                val names = taskRoute.drop(1).split(":")
                val taskId = names.last()

                var current = project
                for (name in names.dropLast(1)) {
                    current = current.subprojects.getByName(name) ?: error("Could not find project :$name")
                }

                current.execute(taskId)
            } else {
                project.execute(id = taskRoute)
            }
        } catch (e: Task.ActException) {
            return
        }
    }
}

fun main(args: Array<String>) {
    val file = File("paddle.yaml")
    if (!file.exists()) {
        Terminal(TextOutput.Console).stderr("Can't find paddle.yaml in root")
        return
    }
    val project = Project.load(file).also {
        it.register(it.plugins.enabled)
    }

    Paddle(project).main(args)
}
