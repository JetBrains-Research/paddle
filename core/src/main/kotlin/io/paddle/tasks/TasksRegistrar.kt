package io.paddle.tasks

import io.paddle.project.Project
import io.paddle.project.config.Configuration

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
