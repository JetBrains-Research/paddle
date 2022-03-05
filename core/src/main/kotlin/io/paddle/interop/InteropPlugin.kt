package io.paddle.interop

import io.paddle.plugin.Plugin
import io.paddle.interop.python.PythonPluginsClient
import io.paddle.project.Project
import io.paddle.tasks.Task
import kotlinx.coroutines.runBlocking

class InteropPlugin(val name: String) : Plugin {
    override fun configure(project: Project) {
        // todo: implement project configure process
    }

    override fun tasks(project: Project): List<Task> = runBlocking {
        PythonPluginsClient.getInfoAboutAvailableTasksFor(name).map {
            InteropTask(project, it.id, it.group, name)
        }
    }

    override fun extensions(project: Project): List<Project.Extension<Any>> {
        // todo: should returns extension for configuration specification
        return emptyList()
    }
}
