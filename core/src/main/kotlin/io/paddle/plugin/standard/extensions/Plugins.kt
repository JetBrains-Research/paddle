package io.paddle.plugin.standard.extensions

import io.paddle.plugin.Plugin
import io.paddle.plugin.repository.SingleJarPluginsRepository
import io.paddle.plugin.repository.StandardPluginsRepository
import io.paddle.plugin.standard.StandardPlugin
import io.paddle.project.PaddleProject
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import java.io.File

val PaddleProject.plugins: Plugins
    get() = extensions.get(Plugins.Extension.key)!!

class Plugins(val enabled: List<Plugin>, val namesOfAvailable: List<String>) {
    object Extension : PaddleProject.Extension<Plugins> {
        override val key: Extendable.Key<Plugins> = Extendable.Key()

        override fun create(project: PaddleProject): Plugins {
            val pluginsConfig = object : ConfigurationView("plugins", project.config) {
                val pluginsIds by list<String>("enabled", emptyList())
                val jarNames by list<String>("jars", emptyList())
            }
            val jarsReps = pluginsConfig.jarNames.map { SingleJarPluginsRepository(File(it)) }

            val namesOfAvailablePlugins = jarsReps.flatMap { it.getAvailablePluginsIds() } +
                StandardPluginsRepository.getAvailablePluginsIds()

            val pluginsToEnable = (jarsReps + StandardPluginsRepository)
                .flatMap { it.getPluginsBy(pluginsConfig.pluginsIds) }

            return Plugins(listOf(StandardPlugin) + pluginsToEnable, namesOfAvailablePlugins)
        }
    }
}
