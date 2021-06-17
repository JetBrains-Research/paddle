package io.paddle.tasks

import io.paddle.schema.PaddleSchema
import io.paddle.tasks.env.CleanTask
import io.paddle.tasks.env.VenvTask
import io.paddle.tasks.exec.ExecTask
import io.paddle.tasks.linter.MyPyTask
import io.paddle.tasks.linter.PyLintTask
import io.paddle.tasks.tests.PyTestTask

object TasksRegistrar {
    private val tasks = HashMap<String, Task>()

    fun default(config: PaddleSchema) {
        register(
            VenvTask(config),
            CleanTask(config),
            MyPyTask(config),
            PyLintTask(config),
            PyTestTask(config)
        )

        for (execution in config.tasks.execution) {
            register(ExecTask(execution, config))
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
}
