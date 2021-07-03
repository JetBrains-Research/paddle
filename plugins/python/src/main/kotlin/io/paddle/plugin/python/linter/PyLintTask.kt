package io.paddle.plugin.python.linter

import io.paddle.project.Project
import io.paddle.project.Requirements
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.Hashable
import io.paddle.utils.hashable

class PyLintTask(project: Project) : IncrementalTask(project) {
    override val id = "linter:pylint"

    override val inputs: List<Hashable> = project.roots.sources.map { it.hashable() }

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("venv"))

    override fun initialize() {
        project.requirements.descriptors.add(
            Requirements.Descriptor("pylint", project.config.get<String>("tasks.linter.pylint.version") ?: "2.8.3"),
        )
    }

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
