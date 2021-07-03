package io.paddle.plugin.python

import io.paddle.plugin.Plugin
import io.paddle.project.Project
import io.paddle.project.config.Configuration
import io.paddle.plugin.python.env.CleanTask
import io.paddle.plugin.python.env.VenvTask
import io.paddle.plugin.python.exec.ExecTask
import io.paddle.plugin.python.linter.MyPyTask
import io.paddle.plugin.python.linter.PyLintTask
import io.paddle.plugin.python.tests.PyTestTask

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
