package io.paddle.interop.python

import io.paddle.plugin.Plugin
import io.paddle.interop.InteropPlugin
import io.paddle.plugin.repository.PluginsRepository

object PythonBasedPluginsRepository : PluginsRepository {
    override fun getAvailablePluginsIds(): Set<String> {
        // TODO: implement functionality which retrieves all plugins' ids from repositories (PyPI??)
        return setOf("greeting")
    }

    override fun getPluginBy(id: String): Plugin? {
        // TODO: implement functionality which downloads plugin into local repository by specified id
        return if (id == "greeting") {
            InteropPlugin(id)
        } else null
    }

    override fun getPluginsBy(ids: List<String>): List<Plugin> {
        return ids.mapNotNull { getPluginBy(it) }
    }
}
