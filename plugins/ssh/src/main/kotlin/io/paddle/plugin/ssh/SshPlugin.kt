package io.paddle.plugin.ssh

import io.paddle.plugin.Plugin
import io.paddle.plugin.ssh.extensions.JsonSchema
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.terminal.Terminal

object SshPlugin : Plugin {
    override val id: String = "ssh"

    override fun configure(project: PaddleProject) {
        val executor = project.extensions.get(SshCommandExecutor.Extension.key) ?: return
        project.executor = executor
        project.terminal.stdout("> Executor :ssh: ${Terminal.colored("ENABLED", Terminal.Color.CYAN)}")
    }

    override fun tasks(project: PaddleProject): List<Task> {
        return emptyList()
    }

    override fun extensions(project: PaddleProject): List<PaddleProject.Extension<Any>> {
        if (project.config.get<String?>("executor.type") != "ssh") {
            return listOf(JsonSchema.Extension) as List<PaddleProject.Extension<Any>>
        }
        return listOf(SshCommandExecutor.Extension, JsonSchema.Extension) as List<PaddleProject.Extension<Any>>
    }
}
