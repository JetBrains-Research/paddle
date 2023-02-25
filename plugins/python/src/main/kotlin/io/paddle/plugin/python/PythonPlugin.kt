package io.paddle.plugin.python

import io.paddle.plugin.Plugin
import io.paddle.plugin.python.dependencies.authentication.AuthenticationProvider
import io.paddle.plugin.python.dependencies.index.PyPackageRepositoryIndexer
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.tasks.install.CiTask
import io.paddle.plugin.python.tasks.install.InstallTask
import io.paddle.plugin.python.tasks.install.LockTask
import io.paddle.plugin.python.tasks.lint.MyPyTask
import io.paddle.plugin.python.tasks.lint.PyLintTask
import io.paddle.plugin.python.tasks.publish.TwinePublishTask
import io.paddle.plugin.python.tasks.resolve.ResolveInterpreterTask
import io.paddle.plugin.python.tasks.resolve.ResolveRepositoriesTask
import io.paddle.plugin.python.tasks.resolve.ResolveRequirementsTask
import io.paddle.plugin.python.tasks.run.RunTask
import io.paddle.plugin.python.tasks.test.PyTestTask
import io.paddle.plugin.python.tasks.venv.VenvTask
import io.paddle.plugin.python.tasks.wheel.WheelTask
import io.paddle.plugin.standard.extensions.plugins
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task

val PaddleProject.hasPython: Boolean
    get() = PythonPlugin in this.plugins.enabled

object PythonPlugin : Plugin {
    override val id: String = "python"

    override fun configure(project: PaddleProject) {
        project.rootDir.resolve(AuthConfig.FILENAME).takeIf { it.exists() }?.let {
            project.configurationFiles.add(it)
        }
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
            WheelTask(project),
            TwinePublishTask(project)
        ) + RunTask.from(project) + PyTestTask.from(project)
    }

    @Suppress("UNCHECKED_CAST")
    override fun extensions(project: PaddleProject): List<PaddleProject.Extension<Any>> {
        return listOf(
            PyPackageRepositoryIndexer.Extension,
            AuthenticationProvider.Extension,
            PythonRegistry.Extension,
            PyLocations.Extension,
            GlobalCacheRepository.Extension,
            AuthConfig.Extension,
            Requirements.Extension,
            Repositories.Extension,
            Environment.Extension,
            Interpreter.Extension,
            JsonSchema.Extension,
            BuildEnvironment.Extension,
            PublishEnvironment.Extension,
            Metadata.Extension,
            PythonCliConfig.Extension,
        ) as List<PaddleProject.Extension<Any>>
    }
}
