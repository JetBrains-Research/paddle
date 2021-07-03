package io.paddle.plugin.standard

import io.paddle.plugin.Plugin
import io.paddle.project.Project
import io.paddle.project.RootsExtension
import io.paddle.tasks.Task

object StandardPlugin: Plugin {
    override fun tasks(project: Project): List<Task> {
        return emptyList()
    }

    @Suppress("UNCHECKED_CAST")
    override fun extensions(project: Project): List<Project.Extension<Any>> {
        return listOf(
            RootsExtension
        ) as List<Project.Extension<Any>>
    }
}
