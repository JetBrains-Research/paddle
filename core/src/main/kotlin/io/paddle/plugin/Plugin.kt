package io.paddle.plugin

import io.paddle.project.Project

interface Plugin {
    fun install(project: Project)
}
