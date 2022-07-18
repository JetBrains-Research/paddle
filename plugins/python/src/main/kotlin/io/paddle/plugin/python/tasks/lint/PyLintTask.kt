package io.paddle.plugin.python.tasks.lint

import io.paddle.plugin.python.PyDevPackageDefaultVersions
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
        get() = listOf(project.roots.sources.hashable())

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("install"))

    override fun initialize() {
        project.requirements.findByName("pylint")
            ?: project.requirements.descriptors.add(
                Requirements.Descriptor(
                    name = "pylint",
                    versionSpecifier = PyDevPackageDefaultVersions.PYLINT,
                    type = Requirements.Descriptor.Type.DEV
                )
            )
        project.tasks.clean.locations.add(File(project.workDir, ".pylint_cache"))
    }

    override fun act() {
        val files = project.roots.sources.walkTopDown().asSequence().filter { file -> file.absolutePath.endsWith(".py") }
        var anyFailed = false
        for (file in files) {
            project.environment.runModule("pylint", listOf(file.absolutePath)).orElseDo { anyFailed = true }
        }
        if (anyFailed) throw ActException("PyLint linting has failed")
    }
}
