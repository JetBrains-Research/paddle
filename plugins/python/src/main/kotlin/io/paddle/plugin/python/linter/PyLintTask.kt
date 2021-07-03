package io.paddle.plugin.python.linter

import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.terminal.Terminal
import io.paddle.utils.Hashable
import io.paddle.utils.hashable
import java.io.File

class PyLintTask(project: Project) : IncrementalTask(project) {
    override val id = "linter:pylint"

    override val inputs: List<Hashable> = project.roots.sources.map { it.hashable() }

    override fun act() {
        val files = project.roots.sources.flatMap { it.walkTopDown().asSequence().filter { file -> file.absolutePath.endsWith(".py") } }
        var anyFailed = false
        for (file in files) {
            val code = project.environment.runModule("pylint", listOf(file.absolutePath))
            anyFailed = anyFailed || code != 0
        }
        if (anyFailed) throw ActException("PyLint linting has failed")
    }
}
