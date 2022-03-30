package io.paddle.plugin.standard.tasks

import io.paddle.project.Project
import io.paddle.project.Tasks
import io.paddle.tasks.Task
import io.paddle.utils.deleteRecursivelyWithoutSymlinks
import io.paddle.utils.tasks.TaskDefaultGroups
import java.io.File

class CleanTask(project: Project) : Task(project) {
    var locations = ArrayList<File>()

    override val id: String = "clean"

    override val group: String = TaskDefaultGroups.BUILD

    override fun initialize() {
        locations.add(File(project.workDir, ".paddle"))
    }

    override fun act() {
        for (location in locations) {
            location.deleteRecursivelyWithoutSymlinks()
        }
    }
}

val Tasks.clean: CleanTask
    get() = this.getOrFail("clean") as CleanTask
