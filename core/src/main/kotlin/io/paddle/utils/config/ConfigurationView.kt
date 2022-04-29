package io.paddle.utils.config

open class ConfigurationView(private val prefix: String = "", private val inner: Configuration) : Configuration() {
    override fun <T> get(key: String): T? {
        return inner.get("$prefix.$key")
    }
}
