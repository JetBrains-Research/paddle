package io.paddle.idea.utils

import io.paddle.interop.GrpcServer
import io.paddle.interop.ProjectsDataProviderService
import io.paddle.plugin.standard.extensions.plugins
import io.paddle.project.Project
import io.paddle.terminal.TextOutput
import io.paddle.utils.config.Configuration
import java.io.File

object PaddleProject {
    private val service = ProjectsDataProviderService()

    var currentProject: Project? = null

    init {
        GrpcServer(port = 50051, service = service).start()
        // todo: start python plugin service
    }

    fun load(file: File, workDir: File, output: TextOutput = TextOutput.Console): Project {
        val config = Configuration.from(file)
        val project = Project(config, workDir, output)
        service.register(project)
        project.register(project.plugins.enabled)
        return project.also { currentProject = it }
    }
}
