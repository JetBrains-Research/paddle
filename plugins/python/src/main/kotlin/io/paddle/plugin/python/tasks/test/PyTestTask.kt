package io.paddle.plugin.python.tasks.test

import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.standard.tasks.clean
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.utils.tasks.TaskDefaultGroups
import java.io.File

class PyTestTask(project: PaddleProject) : Task(project) {
    override val id: String = "pytest"

    override val group: String = TaskDefaultGroups.TEST

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("install"))

    override fun initialize() {
        project.requirements.descriptors.add(Requirements.Descriptor("pytest", project.pytest.version))
        project.tasks.clean.locations.add(File(project.workDir, ".pytest_cache"))
    }

    override fun act() {
        if (project.pytest.targets.isEmpty()) {
            project.terminal.error("No pytest targets detected. Stopping...")
            throw ActException("Pytest tests has failed.")
        } else {
            project.terminal.info("Pytest targets detected: ${project.pytest.targets}")
        }

        val args = project.pytest.additionalArguments.toMutableList()
        project.pytest.keywords?.let { args.apply { add("-k"); add(it) } }
        project.pytest.targets.map { it.absolutePath }.forEach { args.add(it) }

        project.environment.runModule("pytest", args).orElse {
            throw ActException("Pytest tests has failed.")
        }
    }
}
