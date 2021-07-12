package io.paddle.plugin.python.tasks.linter

import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.standard.extensions.roots
import io.paddle.plugin.standard.tasks.clean
import io.paddle.project.*
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.Hashable
import io.paddle.utils.hashable
import io.paddle.utils.tasks.TaskDefaultGroups
import java.io.File

class PyLintTask(project: Project) : IncrementalTask(project) {
    override val id = "pylint"

    override val group: String = TaskDefaultGroups.LINT

    override val inputs: List<Hashable> = project.roots.sources.map { it.hashable() }

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("venv"))

    override fun initialize() {
        project.requirements.descriptors.add(
            Requirements.Descriptor("pylint", project.config.get<String>("tasks.linter.pylint.version") ?: "2.8.3"),
        )
        project.tasks.clean.locations.add(File(project.workDir, ".pylint_cache"))
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
