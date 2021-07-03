package io.paddle.plugin.python.tasks.env

import io.paddle.project.Project
import io.paddle.plugin.python.extensions.environment
import io.paddle.tasks.Task
import java.io.File

class CleanTask(project: Project) : Task(project) {
    private val caches = listOf(
        ".mypy_cache",
        ".pylint_cache",
        ".pytest_cache",
        ".paddle"
    )

    override val id: String = "clean"

    override fun initialize() {}

    override fun act() {
        project.environment.venv.deleteRecursively()
        for (cache in caches) {
            File(cache).deleteRecursively()
        }
    }
}
