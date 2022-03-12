package io.paddle.utils.config

open class ConfigurationView(private val prefix: String, private val inner: Configuration) : Configuration() {
    override fun <T> get(key: String): T? {
        val path =  if (key.isBlank()) prefix else "$prefix.$key"
        return inner.get(path)
    }
}
