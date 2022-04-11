package io.paddle.plugin.pyinjector.extensions

import io.paddle.plugin.LocalPluginsDescriptors
import io.paddle.plugin.pyinjector.dependencies.PyModule
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.dependencies.resolvers.PipResolver
import io.paddle.plugin.python.extensions.Requirements
import io.paddle.project.Project
import io.paddle.utils.config.PluginsConfig
import io.paddle.utils.ext.Extendable

class PyPluginsData(project: Project, pyPackageDescriptors: List<Requirements.Descriptor>, pyModuleDescriptors: List<LocalPluginsDescriptors.Descriptor>) {

    val pyPackages: Collection<PyPackage> = PipResolver.resolve(
        project, pyPackageDescriptors,
        project.pyPluginsRepositories.withPyPackages,
        project.pyPluginsEnvironment.localInterpreterPath
    ) + project.pyPluginsEnvironment.venv.pyPackages

    val pyModules: Collection<PyModule> = resolveModules(project, pyModuleDescriptors)

    object Extension : Project.Extension<PyPluginsData> {
        override val key: Extendable.Key<PyPluginsData> = Extendable.Key()

        override fun create(project: Project): PyPluginsData {
            val config = object : PluginsConfig(project) {
                val plugins: List<Map<String, String>> by plugins(type = "py")
            }
            val descriptors = config.plugins.map {
                Requirements.Descriptor(it["name"]!!, it["version"], it["repository"])
            }

            project.pyPluginsEnvironment.initialize()

            return PyPluginsData(
                project, descriptors,
                project.extensions.get(LocalPluginsDescriptors.Extension.key)?.others ?: emptyList()
            )
        }
    }

    private fun resolveModules(project: Project, descriptions: List<LocalPluginsDescriptors.Descriptor>): List<PyModule> {
        return descriptions.map {
            project.pyPluginsRepositories.withPyModules[it.repoName, it.name]
                ?: throw IllegalArgumentException("Cannot find module with name `${it.name}` inside repository `${it.repoName}`")
        }
    }
}
