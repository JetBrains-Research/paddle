package io.paddle.tasks.exec

import io.paddle.project.Project
import io.paddle.tasks.Task

class ExecTask(name: String, private val entrypoint: String, project: Project) : Task(project) {
    override val id: String = "exec:${name}"

    override val dependencies: List<Task> = listOf(project.tasks.getOrFail("venv"))

    override fun act() {
        val code = project.environment.runScript(entrypoint)
        if (code != 0) throw ActException("Script has returned non-zero exit code: $code")
    }
}
