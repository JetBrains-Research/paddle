package io.paddle.idea.utils

import io.paddle.specification.ConfigurationSpecification
import io.paddle.plugin.standard.extensions.plugins
import io.paddle.project.Project
import io.paddle.terminal.TextOutput
import io.paddle.utils.config.Configuration
import java.io.File

object PaddleProject {
    var currentProject: Project? = null

    fun load(file: File, workDir: File, output: TextOutput = TextOutput.Console): Project {
        val config = Configuration.from(file)
        val configSpec = ConfigurationSpecification.fromResource("/schema/paddle-schema.json")
        val project = Project(config, configSpec, workDir, output).also {
            it.register(it.plugins.enabled)
        }
        return project.also { currentProject = it }
    }
}
