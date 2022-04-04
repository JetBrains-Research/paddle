package io.paddle.plugin.standard

import io.paddle.plugin.Plugin
import io.paddle.plugin.standard.extensions.Descriptor
import io.paddle.plugin.standard.extensions.Roots
import io.paddle.plugin.standard.tasks.CleanTask
import io.paddle.project.Project
import io.paddle.tasks.Task

object StandardPlugin: Plugin {
    override val id: String = "standard"

    override fun configure(project: Project) {
    }

    override fun tasks(project: Project): List<Task> {
        return listOf(
            CleanTask(project)
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun extensions(project: Project): List<Project.Extension<Any>> {
        return listOf(
            Roots.Extension,
            Descriptor.Extension
        ) as List<Project.Extension<Any>>
    }
}
