package io.paddle.plugin.python.tasks.run

import io.paddle.plugin.python.extensions.environment
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.utils.tasks.TaskDefaultGroups

class RunTask(name: String, val entrypoint: String, val arguments: List<String>, project: PaddleProject) : Task(project) {
    val isModuleMode: Boolean
        get() = !entrypoint.endsWith(".py")

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
            isModuleMode -> project.environment.runModule(entrypoint, arguments)
            else -> project.environment.runScript(entrypoint, arguments)
        }.orElse { throw ActException("Script has returned non-zero exit code: $it") }
    }
}
