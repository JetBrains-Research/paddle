package io.paddle.plugin.pyinjector.interop

import io.paddle.plugin.pyinjector.interop.grpc.PyPluginsClient
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.project.Project
import io.paddle.tasks.Task

class PyPackagePlugin(val packageInfo: PyPackage, grpcClient: PyPluginsClient) : PyPlugin(grpcClient) {
    override fun configure(project: Project) {
        TODO("Not yet implemented")
    }

    override fun tasks(project: Project): List<Task> {
        TODO("Not yet implemented")
    }
}
