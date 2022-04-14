package io.paddle.plugin.python.tasks.run

import io.paddle.plugin.python.extensions.environment
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.utils.tasks.TaskDefaultGroups

class RunTask(name: String, private val entrypoint: String, private val arguments: List<String>, project: PaddleProject) : Task(project) {
    companion object {
        fun from(project: PaddleProject): List<RunTask> {
            val configurations = project.config.get<List<Map<String, Any>>?>("tasks.run") ?: return emptyList()
            val tasks = ArrayList<RunTask>()
            for (configuration in configurations) {
                tasks.add(
                    RunTask(
                        configuration["id"] as String,
                        configuration["entrypoint"] as String,
                        configuration.getOrDefault("args", emptyList<String>()) as List<String>,
                        project
                    )
                )
            }
            return tasks
        }
    }

    override val id: String = "run$${name}"

    override val group: String = TaskDefaultGroups.RUN

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("install"))

    override fun initialize() {}

    override fun act() {
        when {
            entrypoint.endsWith(".py") -> project.environment.runScript(entrypoint, arguments)
            else -> project.environment.runModule(entrypoint, arguments)
        }.orElse { throw ActException("Script has returned non-zero exit code: $it") }
    }
}
