package io.paddle.plugin.python

import io.paddle.plugin.Plugin
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.tasks.install.*
import io.paddle.plugin.python.tasks.lint.MyPyTask
import io.paddle.plugin.python.tasks.lint.PyLintTask
import io.paddle.plugin.python.tasks.publish.TwinePublishTask
import io.paddle.plugin.python.tasks.resolve.*
import io.paddle.plugin.python.tasks.run.RunTask
import io.paddle.plugin.python.tasks.setup.BuildTask
import io.paddle.plugin.python.tasks.test.PyTestTask
import io.paddle.plugin.python.tasks.venv.VenvTask
import io.paddle.plugin.standard.extensions.plugins
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task

val PaddleProject.hasPython: Boolean
    get() = PythonPlugin in this.plugins.enabled

object PythonPlugin : Plugin {
    override val id: String = "python"

    override fun configure(project: PaddleProject) {
    }

    override fun tasks(project: PaddleProject): List<Task> {
        return listOf(
            VenvTask(project),
            InstallTask(project),
            ResolveInterpreterTask(project),
            ResolveRequirementsTask(project),
            ResolveRepositoriesTask(project),
            LockTask(project),
            CiTask(project),
            MyPyTask(project),
            PyLintTask(project),
            BuildTask(project),
            TwinePublishTask(project)
        ) + RunTask.from(project) + PyTestTask.from(project)
    }

    @Suppress("UNCHECKED_CAST")
    override fun extensions(project: PaddleProject): List<PaddleProject.Extension<Any>> {
        return listOf(
            AuthConfig.Extension,
            Requirements.Extension,
            Repositories.Extension,
            Environment.Extension,
            Interpreter.Extension,
            JsonSchema.Extension,
            BuildEnvironment.Extension,
            PublishEnvironment.Extension,
            Metadata.Extension,
        ) as List<PaddleProject.Extension<Any>>
    }
}
