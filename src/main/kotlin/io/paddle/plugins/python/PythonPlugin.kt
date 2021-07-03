package io.paddle.plugins.python

import io.paddle.plugins.Plugin
import io.paddle.project.Project
import io.paddle.project.config.Configuration
import io.paddle.plugins.python.env.CleanTask
import io.paddle.plugins.python.env.VenvTask
import io.paddle.plugins.python.exec.ExecTask
import io.paddle.plugins.python.linter.MyPyTask
import io.paddle.plugins.python.linter.PyLintTask
import io.paddle.plugins.python.tests.PyTestTask

object PythonPlugin: Plugin {
    override fun install(project: Project, configuration: Configuration) {
        project.tasks.register(
            CleanTask(project),
            VenvTask(project)
        )

        project.tasks.register(
            MyPyTask(project),
            PyLintTask(project),
            PyTestTask(project)
        )

        for (execution in configuration.tasks.execution) {
            project.tasks.register(ExecTask(execution.id, execution.entrypoint, execution.args, project))
        }
    }
}
