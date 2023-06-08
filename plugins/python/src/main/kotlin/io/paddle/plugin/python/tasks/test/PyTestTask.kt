package io.paddle.plugin.python.tasks.test

import io.paddle.plugin.python.dependencies.pytest.PyTestTarget
import io.paddle.plugin.python.extensions.environment
import io.paddle.plugin.python.extensions.requirements
import io.paddle.plugin.standard.tasks.clean
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.utils.tasks.TaskDefaultGroups

class PyTestTask(
    name: String,
    project: PaddleProject,
    val targets: List<PyTestTarget>,
    val keywords: String?,
    val additionalArgs: List<String>
) : Task(project) {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun from(project: PaddleProject): List<PyTestTask> {
            val configurations = project.config.get<List<Map<String, Any>>?>("tasks.test.pytest") ?: return emptyList()
            val tasks = ArrayList<PyTestTask>()

            for (configuration in configurations) {
                val name = configuration["id"] as String
                val targets = (configuration["targets"] as List<String>? ?: listOf("/")).map {
                    try {
                        PyTestTarget.from(it, project)
                    } catch (e: PyTestTarget.PyTestTargetParseException) {
                        error("Failed to parse pytest targets in ${project.buildFile}: ${e.message ?: "unknown error"}")
                    }
                }

                val keywords = (configuration["keywords"] as String?)
                    ?.takeUnless { it.isBlank() }
                val additionalArgs = (configuration["parameters"] as String?)
                    ?.takeUnless { it.isBlank() }
                    ?.split(" ")
                    ?: emptyList()

                tasks.add(PyTestTask(name, project, targets, keywords, additionalArgs))
            }
            return tasks
        }
    }

    override val id: String = "pytest$$name"

    override val group: String = TaskDefaultGroups.TEST

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("install"))

    override fun initialize() {
        project.tasks.clean.locations.addAll(
            project.workDir.walkTopDown()
                .filter { it.isDirectory && it.name == ".pytest_cache" }
        )
    }

    override fun act() {
        project.requirements.findByName("pytest")
            ?: throw ActException("Package pytest is not installed. Please, add it to the requirements.dev section.")

        if (targets.isEmpty()) {
            project.terminal.error("No pytest targets detected. Stopping...")
            throw ActException("Pytest tests has failed.")
        } else {
            project.terminal.info("Pytest targets detected: $targets")
        }

        val args = additionalArgs.toMutableList()
        keywords?.let { args.apply { add("-k"); add(it) } }
        targets.forEach { args.add(it.cliArgument) }

        project.environment.runModule("pytest", args).orElse {
            throw ActException("Pytest tests has failed.")
        }
    }
}
