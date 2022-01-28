package io.paddle.plugin.remote

import io.paddle.plugin.Plugin
import io.paddle.project.Project
import io.paddle.tasks.Task

class RemotePlugin(val name: String) : Plugin {
    override fun configure(project: Project) {
        // something useful
    }

    override fun tasks(project: Project): List<Task> {
        return RemotePluginsClient.getTasksBy(name).map { RemoteTask(project, it.id, it.group) }
    }

    override fun extensions(project: Project): List<Project.Extension<Any>> {
        // something useful
        return emptyList()
    }
}
