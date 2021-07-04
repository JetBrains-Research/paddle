package io.paddle.plugin.docker

import io.paddle.plugin.Plugin
import io.paddle.project.Project
import io.paddle.tasks.Task

object DockerPlugin: Plugin {
    override fun tasks(project: Project): List<Task> {
        return emptyList()
    }

    override fun extensions(project: Project): List<Project.Extension<Any>> {
        return listOf(DockerWrapper.Extension) as List<Project.Extension<Any>>
    }
}
