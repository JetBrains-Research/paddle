package io.paddle.utils.config

class ConfigurationChain(private val configurations: List<Configuration>) : Configuration() {
    constructor(vararg configurations: Configuration) : this(configurations.toList())

    override fun <T> get(key: String): T? {
        return configurations.firstNotNullOfOrNull { it.get(key) }
    }
}
