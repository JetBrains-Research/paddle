package io.paddle

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import io.paddle.plugin.python.PythonPlugin
import io.paddle.plugin.standard.StandardPlugin
import io.paddle.project.Project
import io.paddle.terminal.TerminalUI
import java.io.File

class Paddle(private val project: Project) : CliktCommand() {
    val task by argument("task", "Use name of task")

    override fun run() {
        project.execute(task)
    }
}

fun main(args: Array<String>) {
    val file = File("paddle.yaml")
    if (!file.exists()) {
        TerminalUI.echoln("Can't find paddle.yaml in root")
        return
    }

    val project = Project.load(file).also {
        it.register(StandardPlugin)
        it.register(PythonPlugin)
    }

    Paddle(project).main(args)
}
