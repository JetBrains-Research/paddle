package io.paddle.plugin.docker

import io.paddle.plugin.Plugin
import io.paddle.plugin.docker.extensions.JsonSchema
import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.terminal.Terminal

object DockerPlugin : Plugin {
    override fun configure(project: Project) {
        val executor = project.extensions.get(DockerCommandExecutor.Extension.key) ?: return
        project.executor = executor
        project.terminal.stdout("> Executor :docker: ${Terminal.colored("ENABLED", Terminal.Color.CYAN)}")
    }

    override fun tasks(project: Project): List<Task> {
        return emptyList()
    }

    override fun extensions(project: Project): List<Project.Extension<Any>> {
        if (project.config.get<String?>("executor.type") != "docker") {
            return listOf(JsonSchema.Extension) as List<Project.Extension<Any>>
        }
        return listOf(DockerCommandExecutor.Extension, JsonSchema.Extension) as List<Project.Extension<Any>>
    }
}
