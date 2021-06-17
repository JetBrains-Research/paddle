package io.paddle.tasks.env

import io.paddle.schema.PaddleSchema
import io.paddle.tasks.Task
import java.io.File

class CleanTask(private val config: PaddleSchema) : Task() {
    private val venv = File(config.environment.virtualenv)

    private val caches = listOf(
        ".mypy_cache",
        ".pylint_cache",
        ".pytest_cache",
        ".paddle"
    )

    override val id: String = "clean"

    override fun act() {
        venv.deleteRecursively()
        for (cache in caches) {
            File(cache).deleteRecursively()
        }
    }
}
