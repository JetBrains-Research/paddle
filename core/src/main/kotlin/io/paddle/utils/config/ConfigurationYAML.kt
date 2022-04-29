package io.paddle.utils.config

import io.paddle.utils.splitAndTrim
import io.paddle.utils.yaml.YAML
import java.io.File


class ConfigurationYAML(private val config: Map<String, Any>) : Configuration() {
    companion object {
        fun from(file: File) = ConfigurationYAML(YAML.parse(file.readText()))
        fun from(yaml: Map<String, Any>) = ConfigurationYAML(yaml)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): T? {
        val parts = key.splitAndTrim(".").takeIf { it.isNotEmpty() } ?: return null

        val path = parts.dropLast(1)
        val name = parts.last()

        var current: Map<String, Any> = config
        for (part in path) {
            current = current[part] as? Map<String, Any> ?: return null
        }

        return current[name] as? T?
    }

    fun toMutableMap() = config.toMutableMap()
}
