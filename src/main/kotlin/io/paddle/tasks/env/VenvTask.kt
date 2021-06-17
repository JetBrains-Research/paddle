package io.paddle.tasks.env

import io.paddle.schema.PaddleSchema
import io.paddle.tasks.Task
import io.paddle.terminal.Terminal
import io.paddle.utils.Hashable
import io.paddle.utils.hashable
import java.io.File

class VenvTask(private val config: PaddleSchema) : Task() {
    private val requirements = File(config.environment.requirements)
    private val venv = File(config.environment.virtualenv)

    override val id: String = "environment:venv"

    override val inputs: List<Hashable> = listOf(requirements.hashable())
    override val outputs: List<Hashable> = listOf(venv.hashable())

    override fun act() {
        var code = Terminal.execute(
            "python3",
            listOf("-m", "venv", venv.absolutePath),
            File(".")
        )
        if (code != 0) throw ActException("VirtualEnv creation has failed")

        code = Terminal.execute(
            "${venv.absolutePath}/bin/pip",
            listOf("install", "-r", File(config.environment.requirements).absolutePath),
            File(".")
        )
        if (code != 0) throw ActException("Requirements.txt installation has failed")
    }
}
