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

class MyPyTask(project: Project) : IncrementalTask(project) {
    override val id: String = "mypy"

    override val group: String = TaskDefaultGroups.LINT

    override val inputs: List<Hashable> = project.roots.sources.map { it.hashable() } + listOf(project.requirements, project.environment.venv.hashable())

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("venv"))

    override fun initialize() {
        project.requirements.descriptors.add(
            Requirements.Descriptor("mypy", project.config.get<String>("tasks.linter.mypy.version") ?: "0.902")
        )
        project.tasks.clean.locations.add(File(project.workDir, ".mypy_cache"))
    }

    override fun act() {
        val files = project.roots.sources.flatMap { it.walkTopDown().asSequence().filter { file -> file.absolutePath.endsWith(".py") } }
        var anyFailed = false
        for (file in files) {
            val code = project.environment.runModule("mypy", listOf(file.absolutePath))
            anyFailed = anyFailed || code != 0
        }
        if (anyFailed) throw ActException("MyPy linting has failed")
    }
}
