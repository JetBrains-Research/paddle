package io.paddle.plugin.standard.tasks

import io.paddle.project.PaddleProject
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.tasks.TaskDefaultGroups

class CleanModuleFilesTask(project: PaddleProject) : IncrementalTask(project) {
    override val id = "cleanIntelliJModuleFiles"

    override val group = TaskDefaultGroups.BUILD

    override fun act() {
        project.rootDir.walkTopDown()
            .filter { it.extension == "iml" }
            .forEach { it.delete() }
    }
}
