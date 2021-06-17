package io.paddle.tasks.linter

import io.paddle.schema.PaddleSchema
import io.paddle.tasks.Task
import io.paddle.tasks.env.VenvTask
import io.paddle.terminal.Terminal
import io.paddle.utils.Hashable
import io.paddle.utils.hashable
import java.io.File

class MyPyTask(private val config: PaddleSchema) : Task() {
    private val requirements = File(config.environment.requirements)
    private val venv = File(config.environment.virtualenv)

    override val id: String = "linter:mypy"

    override val inputs: List<Hashable> =
        config.roots.sources.map { File(it).hashable() } + listOf(requirements.hashable(), venv.hashable())

    override val dependencies: List<Task> = listOf(VenvTask(config))

    override fun act() {
        val roots = config.roots.sources.map { File(it) }
        val files = roots.flatMap { it.walkTopDown().asSequence().filter { file -> file.endsWith(".py") } }
        for (file in files) {
            Terminal.execute("mypy", listOf(file.absolutePath), File("."), redirectStdout = true)
        }
    }
}