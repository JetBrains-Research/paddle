package io.paddle.plugin

import io.paddle.plugin.repositories.EmbeddedPluginsRepository
import io.paddle.plugin.repositories.jarPluginsRepositories
import io.paddle.project.Project
import io.paddle.utils.config.PluginsConfig
import io.paddle.utils.ext.Extendable

val Project.plugins: Plugins
    get() = extensions.get(Plugins.Extension.key)!!

class Plugins(private val _enabled: MutableList<Plugin>) {
    val enabled: List<Plugin>
        get() = _enabled

    object Extension : Project.Extension<Plugins> {
        override val key: Extendable.Key<Plugins> = Extendable.Key()

        override fun create(project: Project): Plugins {
            val config = object : PluginsConfig(project) {
                val embeddedPlugins by plugins<String>(type = "embedded")
            }

            val enabled = mutableListOf(*EmbeddedPluginsRepository.plugins(config.embeddedPlugins + "pyinjector").toTypedArray())
            project.extensions.get(LocalPluginsDescriptors.Extension.key)?.forJarPlugins?.forEach {
                project.jarPluginsRepositories[it.repoName, it.name]?.apply {
                    enabled.add(this@apply)
                } ?: throw IllegalArgumentException("Cannot find jar plugin with name `${it.name}` inside repository `${it.repoName}`")
            }
            return Plugins(enabled)
        }
    }

    fun enableAndRegister(project: Project, plugin: Plugin) {
        enable(plugin)
        project.register(plugin)
    }

    fun enableAndRegister(project: Project, plugins: Collection<Plugin>) {
        plugins.forEach { enableAndRegister(project, it) }
    }


    fun enable(plugin: Plugin) {
        _enabled.add(plugin)
    }

    fun enable(plugins: Collection<Plugin>) {
        plugins.forEach {
            enable(it)
        }
    }
}


class LocalPluginsDescriptors(val forJarPlugins: List<Descriptor>, val others: List<Descriptor>) {
    object Extension : Project.Extension<LocalPluginsDescriptors> {
        override val key: Extendable.Key<LocalPluginsDescriptors> = Extendable.Key()

        override fun create(project: Project): LocalPluginsDescriptors {
            val config = object : PluginsConfig(project) {
                val localPlugins by plugins<Map<String, String>>(type = "local")
            }

            val (jars, others) = config.localPlugins.map {
                val name = it["name"]
                requireNotNull(name)
                val repoName = it["repository"]
                requireNotNull(repoName)
                Descriptor(name, repoName)
            }.partition {
                project.jarPluginsRepositories.contains(it.repoName)
            }

            return LocalPluginsDescriptors(jars, others)
        }
    }

    data class Descriptor(val name: String, val repoName: String)
}
