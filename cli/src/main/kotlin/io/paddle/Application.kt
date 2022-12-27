package io.paddle

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import io.paddle.project.PaddleProjectProvider
import io.paddle.project.extensions.descriptor
import io.paddle.tasks.Task
import java.io.File
import kotlin.system.exitProcess

class Paddle : CliktCommand() {
    private val taskRoute by argument(
        name = "task",
        help = "Use full name of the task to run it (e.g., ':subproject:clean') " +
            "OR short name to run the task for the root project (e.g., 'clean')"
    )

    override fun run() {
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

                current.execute(taskId)
            } else {
                project.execute(taskId = taskRoute)
            }
        } catch (e: Task.ActException) {
            exitProcess(1)
        }
    }
}

fun main(args: Array<String>) {
    Paddle().main(args)
}
