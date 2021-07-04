package io.paddle.plugin.standard.tasks

import io.paddle.project.Project
import io.paddle.project.Tasks
import io.paddle.tasks.Task
import java.io.File

class CleanTask(project: Project) : Task(project) {
    var locations = ArrayList<File>()

    override val id: String = "clean"

    override fun initialize() {
        locations.add(File(".paddle"))
    }

    override fun act() {
        for (location in locations) {
            location.deleteRecursively()
        }
    }
}

val Tasks.clean: CleanTask
    get() = this.getOrFail("clean") as CleanTask
