package io.paddle.interop

import com.google.protobuf.Empty
import io.paddle.plugin.interop.PrintRequest
import io.paddle.plugin.interop.ProjectGrpcKt
import io.paddle.project.Project
import io.paddle.terminal.Terminal

typealias ProjectDir = String

class ProjectsDataProviderService : ProjectGrpcKt.ProjectCoroutineImplBase() {
    private val projects = mutableMapOf<ProjectDir, Project>()

    private var project: Project? = null

    fun register(project: Project) {
        this.project = project
        projects[project.workDir.canonicalPath] = project
    }

    fun register(projects: Collection<Project>) {
        projects.forEach {
            this.projects[it.workDir.canonicalPath] = it
        }
    }

    override suspend fun printMessage(request: PrintRequest): Empty {
        // todo: add error handling
        project?.terminal?.apply {
            stderr(Terminal.colored(request.message, Terminal.Color.valueOf(request.color)))
        }
        return Empty.getDefaultInstance()
    }
}
