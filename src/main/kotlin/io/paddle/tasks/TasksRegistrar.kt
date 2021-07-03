package io.paddle.tasks

import io.paddle.project.Project
import io.paddle.project.config.Configuration
import io.paddle.plugins.python.env.CleanTask
import io.paddle.plugins.python.env.VenvTask
import io.paddle.plugins.python.exec.ExecTask
import io.paddle.plugins.python.linter.MyPyTask
import io.paddle.plugins.python.linter.PyLintTask
import io.paddle.plugins.python.tests.PyTestTask

class TasksRegistrar {
    private val tasks = HashMap<String, Task>()

    fun default(project: Project, configuration: Configuration) {
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
