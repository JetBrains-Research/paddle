package io.paddle.tasks.linter

import io.paddle.schema.PaddleSchema
import io.paddle.tasks.Task
import io.paddle.terminal.Terminal
import io.paddle.utils.Hashable
import io.paddle.utils.hashable
import java.io.File

class PyLintTask(private val config: PaddleSchema) : Task() {
    override val id = "linter:pylint"

    override val inputs: List<Hashable> = config.roots.sources.map { File(it).hashable() }

    override fun act() {
        val roots = config.roots.sources.map { File(it) }
        val files = roots.flatMap { it.walkTopDown().asSequence().filter { file -> file.endsWith(".py") } }
        var anyFailed = false
        for (file in files) {
            val code = Terminal.execute("pylint", listOf(file.absolutePath), File("."), redirectStdout = true)
            anyFailed = anyFailed || code != 0
        }
        if (anyFailed) throw ActException("PyLint linting has failed")
    }
}
