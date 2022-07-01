package io.paddle.plugin.docker

import io.paddle.plugin.Plugin
import io.paddle.plugin.docker.extensions.JsonSchema
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.terminal.Terminal

object DockerPlugin : Plugin {
    override val id: String = "docker"

    override fun configure(project: PaddleProject) {
        val executor = project.extensions.get(DockerCommandExecutor.Extension.key) ?: return
        project.executor = executor
        project.terminal.stdout("> Executor :docker: ${Terminal.colored("ENABLED", Terminal.Color.CYAN)}")
    }

    override fun tasks(project: PaddleProject): List<Task> {
        return emptyList()
    }

    override fun extensions(project: PaddleProject): List<PaddleProject.Extension<Any>> {
        if (project.config.get<String?>("executor.type") != "docker") {
            return listOf(JsonSchema.Extension) as List<PaddleProject.Extension<Any>>
        }
        return listOf(DockerCommandExecutor.Extension, JsonSchema.Extension) as List<PaddleProject.Extension<Any>>
    }
}
