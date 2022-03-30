package io.paddle.plugin.python

import io.paddle.plugin.Plugin
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.tasks.env.*
import io.paddle.plugin.python.tasks.exec.RunTask
import io.paddle.plugin.python.tasks.linter.MyPyTask
import io.paddle.plugin.python.tasks.linter.PyLintTask
import io.paddle.plugin.python.tasks.migrate.ParseRequirementsTxtTask
import io.paddle.plugin.python.tasks.resolve.*
import io.paddle.plugin.python.tasks.tests.PyTestTask
import io.paddle.plugin.standard.tasks.CleanTask
import io.paddle.project.Project
import io.paddle.specification.ConfigSpecView
import io.paddle.specification.tree.*
import io.paddle.tasks.Task
import io.paddle.utils.config.ConfigurationView

object PythonPlugin : Plugin {
    override fun configure(project: Project) {
        val plugins = object : ConfigurationView("plugins", project.config) {
            val enabled by list<String>("enabled", emptyList())
        }
        if (plugins.enabled.contains("python")) {
            project.configSpec.get<CompositeSpecTreeNode>("")!!.children["environment"] =
                CompositeSpecTreeNode(
                    description = "Environment that should be used by Paddle for Python build process",
                    namesOfRequired = mutableListOf("type"),
                    children = mutableMapOf("type" to StringSpecTreeNode(validValues = mutableListOf("virtualenv", "global")))
                )
            if (project.config.get<String>("environment.type") == "virtualenv") {
                val environment = object : ConfigSpecView("environment", project.configSpec) {}
                environment.get<CompositeSpecTreeNode>("")?.run {
                    namesOfRequired!!.add("path")
                    children["path"] = StringSpecTreeNode(description = "Path to virtual environment location")
                }
            }

            project.configSpec.get<CompositeSpecTreeNode>("")!!.children["requirements"] =
                CompositeSpecTreeNode(
                    children = mutableMapOf(
                        "file" to StringSpecTreeNode(),
                        "libraries" to ArraySpecTreeNode(
                            items = CompositeSpecTreeNode(
                                namesOfRequired = mutableListOf("name", "version"),
                                children = mutableMapOf("name" to StringSpecTreeNode(), "version" to StringSpecTreeNode())
                            )
                        )
                    )
                )
        }
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
            Interpreter.Extension
        ) as List<Project.Extension<Any>>
    }
}
