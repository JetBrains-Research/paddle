package io.paddle

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.*
import io.paddle.project.PaddleProjectProvider
import io.paddle.project.extensions.descriptor
import io.paddle.tasks.Task
import java.io.File
import kotlin.system.exitProcess

class Paddle internal constructor() : CliktCommand() {
    private val taskRoute by argument(
        name = "task",
        help = "Use full name of the task to run it (e.g., ':subproject:clean') " +
            "OR short name to run the task for the root project (e.g., 'clean')"
    )
    private val pluginArguments: Map<String, String> by option("-P",
        help = "Plugin specific options. For example -Ppython.run.extraArgs=\"arg1 arg2\""
    ).associate()

    fun runPaddle() {
        println("Loading Paddle project model...")
        val workDir = File(".").canonicalFile
        val project = PaddleProjectProvider.getInstance(rootDir = workDir).getProject(workDir)
            ?: throw IllegalStateException("Internal error: could not load project from ${workDir.canonicalPath}")

        try {
            if (taskRoute.startsWith(':')) {
                val names = taskRoute.drop(1).split(":")
                val taskId = names.last()

                var current = project
                for (name in names.dropLast(1)) {
                    current = current.subprojects.getByName(name) ?: error("Could not find project :$name among subprojects of the current project :${current.descriptor.name}")
                }

                current.execute(taskId, cliArgs = pluginArguments)
            } else {
                project.execute(taskId = taskRoute, cliArgs = pluginArguments)
            }
        } catch (e: Task.ActException) {
            exitProcess(1)
        }
    }
    override fun run() = Unit

    companion object {
        fun parseCliOptions(args: List<String>): Map<String, String> = Paddle().run { main(args); pluginArguments  }
    }
}

internal fun main(args: Array<String>) {
    val app = Paddle()
    app.main(args)
    app.runPaddle()
}
