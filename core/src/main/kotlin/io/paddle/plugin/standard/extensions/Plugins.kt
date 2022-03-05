package io.paddle.plugin.standard.extensions

import io.paddle.plugin.Plugin
import io.paddle.interop.python.PythonBasedPluginsRepository
import io.paddle.plugin.repository.*
import io.paddle.plugin.standard.StandardPlugin
import io.paddle.project.Project
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import java.io.File

val Project.plugins: Plugins
    get() = extensions.get(Plugins.Extension.key)!!

class Plugins(val enabled: List<Plugin>, val namesOfAvailable: List<String>) {
    object Extension : Project.Extension<Plugins> {
        override val key: Extendable.Key<Plugins> = Extendable.Key()

        override fun create(project: Project): Plugins {
            val pluginsConfig = object : ConfigurationView("plugins", project.config) {
                val pluginsIds by list<String>("enabled", emptyList())
                val jarNames by list<String>("jars", emptyList())
            }
            val jarsReps = pluginsConfig.jarNames.map { SingleJarPluginsRepository(File(it)) }

            val namesOfAvailablePlugins = jarsReps.flatMap { it.getAvailablePluginsIds() } +
                StandardPluginsRepository.getAvailablePluginsIds() + PythonBasedPluginsRepository.getAvailablePluginsIds()

            val pluginsToEnable = (jarsReps + StandardPluginsRepository + PythonBasedPluginsRepository)
                .flatMap { it.getPluginsBy(pluginsConfig.pluginsIds) }

            return Plugins(listOf(StandardPlugin) + pluginsToEnable, namesOfAvailablePlugins)
        }
    }
}
