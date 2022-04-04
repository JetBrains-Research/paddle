package io.paddle.plugin.python

import io.paddle.plugin.Plugin
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.tasks.env.*
import io.paddle.plugin.python.tasks.exec.RunTask
import io.paddle.plugin.python.tasks.lint.MyPyTask
import io.paddle.plugin.python.tasks.lint.PyLintTask
import io.paddle.plugin.python.tasks.migrate.ParseRequirementsTxtTask
import io.paddle.plugin.python.tasks.resolve.*
import io.paddle.plugin.python.tasks.test.PyTestTask
import io.paddle.plugin.standard.extensions.plugins
import io.paddle.plugin.standard.tasks.CleanTask
import io.paddle.project.Project
import io.paddle.tasks.Task

val Project.hasPython: Boolean
    get() = PythonPlugin in this.plugins.enabled

object PythonPlugin : Plugin {
    override val id: String = "python"

    override fun configure(project: Project) {
    }

    override fun tasks(project: Project): List<Task> {
        return listOf(
            CleanTask(project),
            VenvTask(project),
            InstallTask(project),
            ResolveInterpreterTask(project),
            ResolveRequirementsTask(project),
            ResolveRepositoriesTask(project),
            LockTask(project),
            CiTask(project),
            MyPyTask(project),
            PyLintTask(project),
            PyTestTask(project),
            ParseRequirementsTxtTask(project)
        ) + RunTask.from(project)
    }

    @Suppress("UNCHECKED_CAST")
    override fun extensions(project: Project): List<Project.Extension<Any>> {
        return listOf(
            Requirements.Extension,
            Repositories.Extension,
            Environment.Extension,
            Interpreter.Extension,
            JsonSchema.Extension
        ) as List<Project.Extension<Any>>
    }
}
