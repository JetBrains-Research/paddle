package io.paddle.plugin.remote

import io.paddle.project.Project
import io.paddle.tasks.Task

class RemoteTask(project: Project, override val id: String, override val group: String) : Task(project) {
    override fun initialize() {
        // something useful
    }

    override fun act() {
        val status = RemotePluginsClient.runTaskBy(id)
        project.terminal.stdout("Running task $id... Result status $status")
    }
}
