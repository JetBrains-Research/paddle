package io.paddle.tasks.tests

import io.paddle.schema.PaddleSchema
import io.paddle.tasks.Task
import io.paddle.tasks.env.VenvTask
import io.paddle.terminal.Terminal
import java.io.File

class PyTestTask(private val config: PaddleSchema) : Task("test:pytest", dependencies = listOf(VenvTask(config))) {
    override fun act() {
        val roots = config.roots.tests.map { File(it) }
        for (file in roots) {
            Terminal.execute("pytest", listOf(file.absolutePath), File("."), redirectStdout = true)
        }
    }
}