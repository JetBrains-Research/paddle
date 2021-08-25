package io.paddle.plugin.repository

import io.paddle.plugin.Plugin

interface PluginsRepository {
    fun getAvailablePluginsIds(): Set<String>
    fun getPluginBy(id: String): Plugin?
    fun getPluginsBy(ids: List<String>): List<Plugin>
}
