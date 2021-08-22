package io.paddle.plugin.standard.extensions

import io.paddle.plugin.Plugin
import io.paddle.plugin.standard.StandardPlugin
import io.paddle.project.Project
import io.paddle.utils.config.Configuration
import io.paddle.utils.ext.Extendable
import java.io.File
import java.util.*
import kotlin.io.path.createTempFile
import kotlin.io.path.outputStream

val Project.plugins: Plugins
    get() = extensions.get(Plugins.Extension.key)!!

class Plugins(val enabled: List<Plugin>) {
    object Extension : Project.Extension<Plugins> {
        override val key: Extendable.Key<Plugins> = Extendable.Key()

        override fun create(project: Project): Plugins {
            val pluginsToEnable = object : Configuration() {
                val ids by list<String>("plugins", emptyList())

                override fun <T> get(key: String): T? {
                    return project.config.get(key)
                }
            }
            val standardAvailablePlugins = getAvailablePluginsFrom(getFileFromResourcesBy(standardConfigPath))
            val plugins = LinkedList<Plugin>(listOf(StandardPlugin)).also {
                pluginsToEnable.ids.forEach { id ->
                    val classname = standardAvailablePlugins[id]
                    if (!classname.isNullOrBlank()) it.add(getPluginObjectBy(classname))
                }
            }
            return Plugins(plugins)
        }

        private fun getAvailablePluginsFrom(configFile: File): Map<String, String> {
            val pluginsConfig = Configuration.from(configFile)
            val configView = object : Configuration() {
                val idsAndClasses by list<Map<String, String>>("plugins", emptyList())

                override fun <T> get(key: String): T? {
                    return pluginsConfig.get(key)
                }
            }
            return hashMapOf<String, String>().also {
                configView.idsAndClasses.forEach { plugin -> it[plugin["id"]!!] = plugin["class"]!! }
            }
        }
    }

    companion object {
        private const val standardConfigPath = "/META-INF/plugins.yaml"

        private fun getFileFromResourcesBy(name: String): File {
            val tempFile = createTempFile()
            this::class.java.getResourceAsStream(name)!!.copyTo(tempFile.outputStream())
            return tempFile.toFile().also { it.deleteOnExit() }
        }

        private fun getPluginObjectBy(classname: String): Plugin {
            return Class.forName(classname).kotlin.objectInstance as Plugin
        }
    }
}
