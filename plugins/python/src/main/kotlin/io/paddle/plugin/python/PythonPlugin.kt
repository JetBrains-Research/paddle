package io.paddle.plugin.python

import io.paddle.plugin.Plugin
import io.paddle.plugin.python.env.CleanTask
import io.paddle.plugin.python.env.VenvTask
import io.paddle.plugin.python.linter.MyPyTask
import io.paddle.plugin.python.linter.PyLintTask
import io.paddle.plugin.python.tests.PyTestTask
import io.paddle.project.Project
import io.paddle.tasks.Task

object PythonPlugin : Plugin {
    override fun tasks(project: Project): List<Task> {
        return listOf(
            CleanTask(project),
            VenvTask(project)
        ) + listOf(
            MyPyTask(project),
            PyLintTask(project),
            PyTestTask(project)
        )
    }
//        for (execution in configuration.tasks.execution) {
//            project.tasks.register(ExecTask(execution.id, execution.entrypoint, execution.args, project))
//        }
}
