package io.paddle.plugin.python.tasks.exec

import io.paddle.project.Project
import io.paddle.plugin.python.extensions.environment
import io.paddle.tasks.Task
import io.paddle.utils.tasks.TaskDefaultGroups

class RunTask(name: String, private val entrypoint: String, private val arguments: List<String>, project: Project) : Task(project) {
    companion object {
        fun from(project: Project): List<RunTask> {
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

    override val id: String = "run:${name}"

    override val group: String = TaskDefaultGroups.APP

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("venv"))

    override fun initialize() {}

    override fun act() {
        val code = when {
            entrypoint.endsWith(".py") -> project.environment.runScript(entrypoint, arguments)
            else -> project.environment.runModule(entrypoint, arguments)
        }
        if (code != 0) throw ActException("Script has returned non-zero exit code: $code")
    }
}
