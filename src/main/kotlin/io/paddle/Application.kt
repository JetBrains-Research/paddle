package io.paddle

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import io.paddle.schema.PaddleSchema
import io.paddle.tasks.TasksRegistrar
import java.io.File

class Paddle : CliktCommand() {
    override fun run() {
        echo("Starting Paddle...")
    }
}

class Task : CliktCommand(help = "Execute specific task", name = "task") {
    val id by argument("id", "Choose the id of task")

    override fun run() {
        val task = TasksRegistrar.get(id)
        if (task == null) {
            echo("Unknown task identifier")
        } else {
            echo("Executing task $id")
            task.run()
        }
    }
}

fun main(args: Array<String>) {
    val config = PaddleSchema.from(File("paddle.yaml"))
    TasksRegistrar.default(config)

    Paddle().subcommands(Task()).main(args)
}