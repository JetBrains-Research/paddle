package io.paddle.tasks

import io.paddle.project.Project
import io.paddle.project.config.Configuration
import io.paddle.tasks.env.CleanTask
import io.paddle.tasks.env.VenvTask
import io.paddle.tasks.exec.ExecTask
import io.paddle.tasks.linter.MyPyTask
import io.paddle.tasks.linter.PyLintTask
import io.paddle.tasks.tests.PyTestTask

class TasksRegistrar {
    private val tasks = HashMap<String, Task>()

    fun default(project: Project, configuration: Configuration) {
        register(
            CleanTask(project),
            VenvTask(project)
        )

        register(
            MyPyTask(project),
            PyLintTask(project),
            PyTestTask(project)
        )

        for (execution in configuration.tasks.execution) {
            register(ExecTask(execution.id, execution.entrypoint, project))
        }
    }

    fun register(vararg given: Task) {
        for (task in given) {
            if (task.id in tasks) return
            tasks[task.id] = task
        }
    }

    fun get(id: String): Task? {
        return tasks[id]
    }

    fun getOrFail(id: String): Task {
        return tasks[id] ?: error("Can't find task $id")
    }
}
