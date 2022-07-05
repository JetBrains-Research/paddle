package io.paddle.plugin.migration

import io.paddle.plugin.Plugin
import io.paddle.plugin.migration.tasks.migrate.CreateDefaultBuildFilesTask
import io.paddle.plugin.migration.tasks.migrate.ParseRequirementsTxtTask
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task

object MigrationPlugin : Plugin {
    override val id: String = "migration"

    override fun configure(project: PaddleProject) {
    }

    override fun tasks(project: PaddleProject): List<Task> {
        return listOf(
            CreateDefaultBuildFilesTask(project),
            ParseRequirementsTxtTask(project)
        )
    }

    override fun extensions(project: PaddleProject): List<PaddleProject.Extension<Any>> {
        return emptyList()
    }
}
