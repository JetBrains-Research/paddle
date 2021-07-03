package io.paddle.plugin.python.tasks.tests

import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.*
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.Hashable
import io.paddle.utils.hashable

class PyTestTask(project: Project) : IncrementalTask(project) {
    override val id: String = "test"

    override val inputs: List<Hashable> =
        project.roots.sources.map { it.hashable() } + project.roots.tests.map { it.hashable() } +
            listOf(project.requirements, project.environment.venv.hashable())

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("venv"))

    override fun initialize() {
        project.requirements.descriptors.add(
            Requirements.Descriptor("pytest", project.config.get<String>("tasks.tests.pytest.version") ?: "6.2.4")
        )
    }

    override fun act() {
        var anyFailed = false
        for (file in project.roots.tests) {
            val code = project.environment.runModule("pytest", listOf(file.absolutePath))
            anyFailed = anyFailed || code != 0
        }
        if (anyFailed) throw ActException("PyTest tests has failed")
    }
}
