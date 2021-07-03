package io.paddle.plugins

import io.paddle.project.Project
import io.paddle.project.config.Configuration

interface Plugin {
    fun install(project: Project, configuration: Configuration)
}
