package io.paddle.plugin.repository

import io.paddle.plugin.Plugin
import io.paddle.plugin.remote.RemotePlugin
import io.paddle.plugin.remote.RemotePluginsClient

object RemotePluginsRepository : PluginsRepository {
    private val remotePlugins by lazy { RemotePluginsClient.getPluginIds().toSet() }

    override fun getAvailablePluginsIds(): Set<String> {
        return remotePlugins
    }

    override fun getPluginBy(id: String): Plugin? {
        return if (remotePlugins.contains(id)) RemotePlugin(id) else null
    }

    override fun getPluginsBy(ids: List<String>): List<Plugin> {
        return remotePlugins.intersect(ids.toSet()).map { RemotePlugin(it) }
    }
}
