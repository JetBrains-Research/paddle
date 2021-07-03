package io.paddle.plugin

import io.paddle.project.Project
import io.paddle.tasks.Task

interface Plugin {
    fun tasks(project: Project): List<Task>
}
