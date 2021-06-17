package io.paddle.tasks.linter

import io.paddle.schema.PaddleSchema
import io.paddle.tasks.Task
import io.paddle.tasks.env.VenvTask
import io.paddle.terminal.Terminal
import java.io.File

class MyPyTask(private val config: PaddleSchema) : Task(id = "linter:mypy", dependencies = listOf(VenvTask(config))) {
    override fun act() {
        val roots = config.roots.sources.map { File(it) }
        val files = roots.flatMap { it.walkTopDown().asSequence().filter { file -> file.endsWith(".py") } }
        for (file in files) {
            Terminal.execute("mypy", listOf(file.absolutePath), File("."), redirectStdout = true)
        }
    }
}