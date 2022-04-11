package io.paddle.plugin.pyinjector

import io.paddle.plugin.Plugin
import io.paddle.plugin.plugins
import io.paddle.plugin.pyinjector.extensions.*
import io.paddle.plugin.pyinjector.interop.PyModulePlugin
import io.paddle.plugin.pyinjector.interop.PyPackagePlugin
import io.paddle.plugin.pyinjector.interop.grpc.PyPluginsClient
import io.paddle.project.Project
import io.paddle.specification.ConfigSpecView
import io.paddle.specification.tree.*
import io.paddle.tasks.Task

object PyInjectorPlugin : Plugin {
    override fun configure(project: Project) {
        val configSpec = object : ConfigSpecView("plugins", project.configSpec) {
            val repos by list<CompositeSpecTreeNode>("repositories")
        }

        val repoTypes = configSpec.repos.items
        repoTypes.validValues = repoTypes.validValues ?: mutableListOf()
        repoTypes.validValues!!.add(
            CompositeSpecTreeNode(
                description = "Python packages repository", namesOfRequired = mutableSetOf("name", "url"),
                children = mutableMapOf(
                    "name" to StringSpecTreeNode(),
                    "url" to StringSpecTreeNode(),
                    "secondary" to BooleanSpecTreeNode(),
                    "default" to BooleanSpecTreeNode()
                )
            )
        )
        repoTypes.validValues!!.add(
            CompositeSpecTreeNode(
                description = "Local Python modules repository", namesOfRequired = mutableSetOf("name", "dir"),
                children = mutableMapOf("name" to StringSpecTreeNode(), "dir" to StringSpecTreeNode())
            )
        )
        configSpec.get<CompositeSpecTreeNode>("enabled")!!.children["py"] = ArraySpecTreeNode(
            description = "Plugins from Python's Package repositories",
            items = CompositeSpecTreeNode(
                namesOfRequired = mutableSetOf("name"),
                children = mutableMapOf("name" to StringSpecTreeNode(), "version" to StringSpecTreeNode(), "repository" to StringSpecTreeNode())
            )
        )

        val pyPluginsData = project.extensions.get(PyPluginsData.Extension.key) ?: return
        project.plugins.enableAndRegister(project, pyPluginsData.pyPackages.map { PyPackagePlugin(it, PyPluginsClient) })
        project.plugins.enableAndRegister(project, pyPluginsData.pyModules.map { PyModulePlugin(it, PyPluginsClient) })
        // TODO: startup all necessary services
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
