package io.paddle.interop

import io.paddle.project.Project
import io.paddle.tasks.Task

class InteropTask(project: Project, override val id: String, override val group: String, private val pluginId: String) : Task(project) {
    override fun initialize() {
        TODO("implement")
    }

    override fun act() {
        TODO("implement")
    }
}
