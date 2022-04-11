package io.paddle.plugin.pyinjector

import io.paddle.plugin.Plugin
import io.paddle.plugin.plugins
import io.paddle.plugin.pyinjector.extensions.*
import io.paddle.plugin.pyinjector.interop.PyModulePlugin
import io.paddle.plugin.pyinjector.interop.PyPackagePlugin
import io.paddle.plugin.pyinjector.interop.grpc.PyPluginsClient
import io.paddle.project.Project
import io.paddle.specification.ConfigSpecView
import io.paddle.tasks.Task

object PyInjectorPlugin : Plugin {
    override fun configure(project: Project) {
        val pyPluginsData = project.extensions.get(PyPluginsData.Extension.key) ?: return
        project.plugins.enableAndRegister(project, pyPluginsData.pyPackages.map { PyPackagePlugin(it, PyPluginsClient) })
        project.plugins.enableAndRegister(project, pyPluginsData.pyModules.map { PyModulePlugin(it, PyPluginsClient) })
        // todo: modify configuration and startup all necessary services
    }

    override fun tasks(project: Project): List<Task> = emptyList()

    @Suppress("UNCHECKED_CAST")
    override fun extensions(project: Project): List<Project.Extension<Any>> {
        return listOf(
            PyPluginsInterpreter.Extension,
            PyPluginsEnvironment.Extension,
            PyPluginsRepositories.Extension,
            PyPluginsData.Extension,
        ) as List<Project.Extension<Any>>
    }
}
