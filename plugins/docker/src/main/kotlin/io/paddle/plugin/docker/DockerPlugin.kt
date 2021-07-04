package io.paddle.plugin.docker

import io.paddle.plugin.Plugin
import io.paddle.project.Project
import io.paddle.tasks.Task

object DockerPlugin: Plugin {
    override fun configure(project: Project) {
        val executor = project.extensions.get(DockerCommandExecutor.Extension.key) ?: return
        project.executor = executor
    }
    override fun tasks(project: Project): List<Task> {
        return emptyList()
    }

    override fun extensions(project: Project): List<Project.Extension<Any>> {
        if (project.config.get<String?>("docker.image") == null) {
            return emptyList()
        }
        return listOf(DockerCommandExecutor.Extension) as List<Project.Extension<Any>>
    }
}
