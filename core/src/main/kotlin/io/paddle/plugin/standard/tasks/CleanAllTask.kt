package io.paddle.plugin.standard.tasks

import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.utils.deleteRecursivelyWithoutSymlinks

class CleanAllTask(project: PaddleProject) : CleanTask(project) {
    override val id: String = "cleanAll"

    override val dependencies: List<Task>
        get() = project.subprojects.getAllTasksById(this.id)

    override fun act() {
        project.tasks.clean.locations.forEach { it.deleteRecursivelyWithoutSymlinks() }
    }
}
