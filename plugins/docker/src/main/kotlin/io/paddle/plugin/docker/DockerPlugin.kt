package io.paddle.plugin.docker

import io.paddle.plugin.Plugin
import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.terminal.TerminalUI

object DockerPlugin: Plugin {
    override fun configure(project: Project) {
        val executor = project.extensions.get(DockerCommandExecutor.Extension.key) ?: return
        project.executor = executor
        TerminalUI.echoln("> Executor :docker: ${TerminalUI.colored("ENABLED", TerminalUI.Color.CYAN)}")
    }
    override fun tasks(project: Project): List<Task> {
        return emptyList()
    }

    override fun extensions(project: Project): List<Project.Extension<Any>> {
        if (project.config.get<String?>("executor.type") != "docker") {
            return emptyList()
        }
        return listOf(DockerCommandExecutor.Extension) as List<Project.Extension<Any>>
    }
}
