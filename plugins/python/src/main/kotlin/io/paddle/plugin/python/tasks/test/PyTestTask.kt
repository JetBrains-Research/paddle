package io.paddle.plugin.python.tasks.test

import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.standard.extensions.roots
import io.paddle.plugin.standard.tasks.clean
import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import io.paddle.utils.tasks.TaskDefaultGroups
import java.io.File

class PyTestTask(project: Project) : IncrementalTask(project) {
    override val id: String = "test"

    override val group: String = TaskDefaultGroups.TEST

    override val inputs: List<Hashable> =
        project.roots.sources.map { it.hashable() } + project.roots.tests.map { it.hashable() } +
            listOf(project.requirements, project.environment.venv.hashable())

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("install"))

    override fun initialize() {
        project.requirements.descriptors.add(
            Requirements.Descriptor(
                name = "pytest",
                version = project.config.get<String>("tasks.tests.pytest.version") ?: "6.2.5",
                repo = Repositories.Descriptor.PYPI.name
            )
        )
        project.tasks.clean.locations.add(File(project.workDir, ".pytest_cache"))
    }

    override fun act() {
        var anyFailed = false
        for (file in project.roots.tests) {
            project.environment.runModule("pytest", listOf(file.absolutePath)).orElseDo { anyFailed = true }
        }
        if (anyFailed) throw ActException("PyTest tests has failed")
    }
}
