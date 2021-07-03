package io.paddle.utils.config

import io.paddle.utils.splitAndTrim
import io.paddle.utils.yaml.YAML
import java.io.File


class ConfigurationYAML(file: File) : Configuration() {
    private val config = YAML.parse<Map<String, Any>>(file.readText())

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
}
