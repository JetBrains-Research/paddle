package io.paddle.plugin.pyinjector.interop

import io.paddle.plugin.Plugin
import io.paddle.plugin.pyinjector.interop.grpc.PyPluginsClient
import io.paddle.project.Project

abstract class PyPlugin(protected val grpcClient: PyPluginsClient) : Plugin {
    override fun extensions(project: Project): List<Project.Extension<Any>> {
        /*
        Extensions for Python-based Paddle plugins are not supported because of absent general interface.
        Necessary functionality for tasks must be implemented inside corresponding plugin's python module
        and called inside tasks.
         */
        return emptyList()
    }
}
