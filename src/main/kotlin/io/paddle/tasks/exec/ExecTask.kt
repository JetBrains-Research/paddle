package io.paddle.tasks.exec

import io.paddle.project.Project
import io.paddle.tasks.Task

class ExecTask(name: String, private val entrypoint: String, private val arguments: List<String>, project: Project) : Task(project) {
    override val id: String = "exec:${name}"

    override val dependencies: List<Task> = listOf(project.tasks.getOrFail("venv"))

    override fun act() {
        val code = when {
            entrypoint.endsWith(".py") -> project.environment.runScript(entrypoint, arguments)
            else -> project.environment.runModule(entrypoint, arguments)
        }
        if (code != 0) throw ActException("Script has returned non-zero exit code: $code")
    }
}
