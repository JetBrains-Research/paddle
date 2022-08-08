package io.paddle

import io.paddle.utils.config.*
import kotlin.io.path.readText

object PaddleRegistry : Configuration() {
    private val registryConfig: ConfigurationChain
        get() = PaddleLocations.registry
            .takeIf { it.readText().isNotEmpty() }
            ?.run { ConfigurationYAML.from(toFile()) }
            ?.let { ConfigurationChain(it) }
            ?: ConfigurationChain()

    fun addChild(configuration: Configuration) {
        registryConfig.addChild(configuration)
    }

    override fun <T> get(key: String): T? {
        return registryConfig.get(key)
    }
}
