package io.paddle.utils.config

class ConfigurationChain(private val configurations: MutableList<Configuration>) : Configuration() {
    constructor(vararg configurations: Configuration) : this(configurations.toMutableList())

    fun addChild(configuration: Configuration) {
        configurations.add(configuration)
    }

    override fun <T> get(key: String): T? {
        return configurations.firstNotNullOfOrNull { it.get(key) }
    }
}
