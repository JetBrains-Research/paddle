package io.paddle.idea.utils

import io.paddle.plugin.docker.DockerPlugin
import io.paddle.plugin.python.PythonPlugin
import io.paddle.plugin.ssh.SshPlugin
import io.paddle.plugin.standard.StandardPlugin
import io.paddle.project.Project
import io.paddle.terminal.TextOutput
import io.paddle.utils.config.Configuration
import java.io.File

object PaddleProject {
    fun load(file: File, workDir: File, output: TextOutput = TextOutput.Console): Project {
        val config = Configuration.from(file)
        val project = Project(config, workDir, output).also {
            it.register(StandardPlugin)
            it.register(PythonPlugin)
            it.register(DockerPlugin)
            it.register(SshPlugin)
        }
        return project
    }
}
