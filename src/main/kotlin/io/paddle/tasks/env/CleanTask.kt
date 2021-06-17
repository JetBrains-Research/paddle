package io.paddle.tasks.env

import io.paddle.project.Project
import io.paddle.project.config.Configuration
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

    override fun act() {
        project.environment.venv.deleteRecursively()
        for (cache in caches) {
            File(cache).deleteRecursively()
        }
    }
}
