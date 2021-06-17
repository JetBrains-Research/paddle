package io.paddle.tasks.tests

import io.paddle.schema.PaddleSchema
import io.paddle.tasks.Task
import io.paddle.tasks.env.VenvTask
import io.paddle.terminal.Terminal
import io.paddle.utils.Hashable
import io.paddle.utils.hashable
import java.io.File

class PyTestTask(private val config: PaddleSchema) : Task() {
    private val requirements = File(config.environment.requirements)
    private val venv = File(config.environment.virtualenv)

    override val id: String = "test:pytest"

    override val inputs: List<Hashable> =
        config.roots.sources.map { File(it).hashable() } + listOf(requirements.hashable(), venv.hashable())

    override val dependencies: List<Task> = listOf(VenvTask(config))

    override fun act() {
        val roots = config.roots.tests.map { File(it) }
        var anyFailed = false
        for (file in roots) {
            val code = Terminal.execute("pytest", listOf(file.absolutePath), File("."), redirectStdout = true)
            anyFailed = anyFailed || code != 0
        }
        if (anyFailed) throw ActException("PyTest tests has failed")
    }
}
