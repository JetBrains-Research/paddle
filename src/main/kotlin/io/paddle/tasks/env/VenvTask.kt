package io.paddle.tasks.env

import io.paddle.schema.PaddleSchema
import io.paddle.tasks.Task
import io.paddle.terminal.Terminal
import java.io.File

class VenvTask(private val config: PaddleSchema) : Task("environment:venv") {
    private val venv = File(config.environment.virtualenv)

    override fun act() {
        Terminal.execute(
            "python3",
            listOf("-m", "venv", venv.absolutePath),
            File(".")
        )
        Terminal.execute(
            "${venv.absolutePath}/bin/pip",
            listOf("install", "-r", File(config.environment.requirements).absolutePath),
            File(".")
        )
    }
}