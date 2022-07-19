package io.paddle.plugin.standard

import io.paddle.plugin.Plugin
import io.paddle.plugin.standard.extensions.Plugins
import io.paddle.plugin.standard.extensions.Roots
import io.paddle.plugin.standard.tasks.*
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task

object StandardPlugin: Plugin {
    override val id: String = "standard"

    override fun configure(project: PaddleProject) {
    }

    override fun tasks(project: PaddleProject): List<Task> {
        return listOf(
            CleanTask(project),
            CleanAllTask(project),
        )
    }

    @Suppress("UNCHECKED_CAST")
    override fun extensions(project: PaddleProject): List<PaddleProject.Extension<Any>> {
        return listOf(
            Roots.Extension,
            Plugins.Extension
        ) as List<PaddleProject.Extension<Any>>
    }
}
