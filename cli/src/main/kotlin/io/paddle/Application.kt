package io.paddle

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import io.paddle.project.PaddleProject
import io.paddle.project.PaddleProjectProvider
import io.paddle.tasks.Task
import java.io.File

class Paddle(private val project: PaddleProject) : CliktCommand() {
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
                project.execute(taskId = taskRoute)
            }
        } catch (e: Task.ActException) {
            return
        }
    }
}

fun main(args: Array<String>) {
    val workDir = File(".").canonicalFile
    val project = PaddleProjectProvider.getInstance(rootDir = workDir).getProject(workDir)
        ?: throw IllegalStateException("Internal error: could not load project from ${workDir.canonicalPath}")
    Paddle(project).main(args)
}
