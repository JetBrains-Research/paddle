package io.paddle.tasks.exec

import io.paddle.schema.PaddleSchema
import io.paddle.tasks.Task
import io.paddle.tasks.env.VenvTask
import io.paddle.terminal.Terminal
import java.io.File

class ExecTask(val execution: PaddleSchema.Tasks.Execution, val config: PaddleSchema) : Task() {
    private val venv = File(config.environment.virtualenv)

    override val id: String = "exec:${execution.id}"

    override val dependencies: List<Task> = listOf(VenvTask(config))

    override fun act() {
        val code = Terminal.execute(
            "${venv.absolutePath}/bin/python",
            listOf(execution.entrypoint),
            File(".")
        )
        if (code != 0) throw ActException("Script has returned non-zero exit code: ${code}")
    }
}
