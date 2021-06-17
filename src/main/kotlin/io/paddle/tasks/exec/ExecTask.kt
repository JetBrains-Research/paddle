package io.paddle.tasks.exec

import io.paddle.schema.PaddleSchema
import io.paddle.tasks.Task
import io.paddle.terminal.Terminal
import java.io.File

class ExecTask(val execution: PaddleSchema.Tasks.Execution, val config: PaddleSchema) : Task("exec:${execution.id}") {
    private val venv = File(config.environment.virtualenv)

    override fun act() {
        Terminal.execute(
            "${venv.absolutePath}/bin/python",
            listOf(execution.entrypoint),
            File(".")
        )
    }
}