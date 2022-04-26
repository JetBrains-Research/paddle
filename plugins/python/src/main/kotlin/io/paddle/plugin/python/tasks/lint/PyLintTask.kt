package io.paddle.plugin.python.tasks.lint

import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.standard.extensions.roots
import io.paddle.plugin.standard.tasks.clean
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import io.paddle.utils.tasks.TaskDefaultGroups
import java.io.File

class PyLintTask(project: PaddleProject) : IncrementalTask(project) {
    override val id = "pylint"

    override val group: String = TaskDefaultGroups.LINT

    override val inputs: List<Hashable>
        get() = project.roots.sources.map { it.hashable() }

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("install"))

    override fun initialize() {
        project.requirements.descriptors.add(
            Requirements.Descriptor(
                name = "pylint",
                version = project.config.get<String>("tasks.linter.pylint.version") ?: "2.12.2",
                repo = Repositories.Descriptor.PYPI.name
            ),
        )
        project.tasks.clean.locations.add(File(project.workDir, ".pylint_cache"))
    }

    override fun act() {
        val files = project.roots.sources.flatMap { it.walkTopDown().asSequence().filter { file -> file.absolutePath.endsWith(".py") } }
        var anyFailed = false
        for (file in files) {
            project.environment.runModule("pylint", listOf(file.absolutePath)).orElseDo { anyFailed = true }
        }
        if (anyFailed) throw ActException("PyLint linting has failed")
    }
}
