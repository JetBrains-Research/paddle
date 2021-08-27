package io.paddle.plugin.repository

import io.paddle.plugin.Plugin
import io.paddle.utils.config.Configuration
import java.io.File
import kotlin.io.path.outputStream

abstract class AbstractPluginsRepository : PluginsRepository {
    protected open val configPath = "META-INF/plugins.yaml"
    protected abstract val classLoader: ClassLoader

    private val pluginsIdsToClassnames: Map<String, String> by lazy {
        parseConfigFile()
    }

    private fun parseConfigFile(): Map<String, String> {
        val pluginsConfig = Configuration.from(getConfigFile())
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

    private fun getConfigFile(): File {
        val tempFile = kotlin.io.path.createTempFile()
        classLoader.getResourceAsStream(configPath)!!.copyTo(tempFile.outputStream())
        return tempFile.toFile().also { it.deleteOnExit() }
    }

    override fun getAvailablePluginsIds(): Set<String> {
        return pluginsIdsToClassnames.keys
    }

    override fun getPluginBy(id: String): Plugin? {
        return pluginsIdsToClassnames[id]?.let { loadClassBy(it) }
    }

    private fun loadClassBy(classname: String): Plugin {
        return Class.forName(classname, true, classLoader).kotlin.objectInstance as Plugin
    }

    override fun getPluginsBy(ids: List<String>): List<Plugin> {
        return ids.mapNotNull { getPluginBy(it) }
    }
}
