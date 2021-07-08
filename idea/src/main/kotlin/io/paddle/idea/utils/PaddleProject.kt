package io.paddle.idea.utils

import io.paddle.plugin.docker.DockerPlugin
import io.paddle.plugin.python.PythonPlugin
import io.paddle.plugin.standard.StandardPlugin
import io.paddle.project.Project
import io.paddle.utils.config.Configuration
import java.io.File

object PaddleProject {
    fun load(file: File, workDir: File): Project {
        val config = Configuration.from(file)
        val project = Project(config, workDir).also {
            it.register(StandardPlugin)
            it.register(PythonPlugin)
            it.register(DockerPlugin)
        }
        return project
    }
}
