package io.paddle.plugin.ssh

import io.paddle.plugin.Plugin
import io.paddle.plugin.ssh.extensions.JsonSchema
import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.terminal.Terminal

object SshPlugin : Plugin {
    override fun configure(project: Project) {
        val executor = project.extensions.get(SshCommandExecutor.Extension.key) ?: return
        project.executor = executor
        project.terminal.stdout("> Executor :ssh: ${Terminal.colored("ENABLED", Terminal.Color.CYAN)}")
    }

    override fun tasks(project: Project): List<Task> {
        return emptyList()
    }

    override fun extensions(project: Project): List<Project.Extension<Any>> {
        if (project.config.get<String?>("executor.type") != "ssh") {
            return listOf(JsonSchema.Extension) as List<Project.Extension<Any>>
        }
        return listOf(SshCommandExecutor.Extension, JsonSchema.Extension) as List<Project.Extension<Any>>
    }
}
