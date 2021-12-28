package io.paddle.plugin.python.tasks.linter

import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.standard.extensions.roots
import io.paddle.plugin.standard.tasks.clean
import io.paddle.project.Project
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
        get() = listOf(project.tasks.getOrFail("install"))

    override fun initialize() {
        project.requirements.descriptors.add(
            Requirements.Descriptor(
                name = "mypy",
                version = project.config.get<String>("tasks.linter.mypy.version") ?: "0.902",
                repo = Repositories.Descriptor.PYPI.name
            )
        )
        project.tasks.clean.locations.add(File(project.workDir, ".mypy_cache"))
    }

    override fun act() {
        val files = project.roots.sources.flatMap { it.walkTopDown().asSequence().filter { file -> file.absolutePath.endsWith(".py") } }
        var anyFailed = false
        for (file in files) {
            project.environment.runModule("mypy", listOf(file.absolutePath)).orElseDo { anyFailed = true }
        }
        if (anyFailed) throw ActException("MyPy linting has failed")
    }
}
