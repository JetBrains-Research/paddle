package io.paddle

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import io.paddle.interop.GrpcServer
import io.paddle.interop.ProjectsDataProviderService
import io.paddle.plugin.standard.extensions.plugins
import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.terminal.Terminal
import io.paddle.terminal.TextOutput
import java.io.File

class Paddle(private val project: Project) : CliktCommand() {
    val task by argument("task", "Use name of task")

    override fun run() {
        try {
            project.execute(task)
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
    val project = Project.load(file)
//    GrpcServer(
//        port = 50051,
//        service = ProjectsDataProviderService().apply {
//            register(project)
//        }).start()

    project.register(project.plugins.enabled)

    Paddle(project).main(args)
}
