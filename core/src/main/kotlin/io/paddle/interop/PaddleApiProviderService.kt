package io.paddle.interop

import com.google.protobuf.Empty
import io.paddle.plugin.interop.PrintRequest
import io.paddle.plugin.interop.PrintRequest.Type.*
import io.paddle.plugin.interop.ProjectGrpcKt
import io.paddle.project.Project

typealias ProjectId = String

class PaddleApiProviderService(initProjects: List<Project> = emptyList()) : ProjectGrpcKt.ProjectCoroutineImplBase() {
    private val projects: MutableMap<ProjectId, Project> = initProjects.associateBy { it.id }.toMutableMap()

    fun register(project: Project) {
        projects[project.id] = project
    }

    fun register(projects: Collection<Project>) {
        projects.forEach {
            this.projects[it.id] = it
        }
    }

    override suspend fun printMessage(request: PrintRequest): Empty {
        projects[request.projectId]?.terminal?.apply {
            with(request.message) {
                when (request.type) {
                    DEBUG -> debug(this)
                    INFO -> info(this)
                    WARN -> warn(this)
                    ERROR -> error(this)
                    OUT -> stdout(this)
                    else -> stderr(this)
                }
            }
        }
        return Empty.getDefaultInstance()
    }
}
