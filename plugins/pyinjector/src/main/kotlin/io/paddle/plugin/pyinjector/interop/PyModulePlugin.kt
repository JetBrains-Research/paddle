package io.paddle.plugin.pyinjector.interop

import io.paddle.plugin.pyinjector.dependencies.PyModule
import io.paddle.plugin.pyinjector.interop.grpc.PyPluginsClient
import io.paddle.project.Project
import io.paddle.tasks.Task

class PyModulePlugin(val moduleInfo: PyModule, grpcClient: PyPluginsClient) : PyPlugin(grpcClient) {

    override fun configure(project: Project) {
        TODO("Not yet implemented")
    }

    override fun tasks(project: Project): List<Task> {
        TODO("Not yet implemented")
    }
}
