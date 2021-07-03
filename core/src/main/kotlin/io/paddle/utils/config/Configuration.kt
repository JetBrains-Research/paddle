package io.paddle.utils.config

import java.io.File
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class Configuration {
    @Suppress("UNCHECKED_CAST")
    class ConfigurationDelegate<T>(private val name: String, private val default: T? = null) : ReadOnlyProperty<Configuration, T> {
        override fun getValue(thisRef: Configuration, property: KProperty<*>): T {
            return (thisRef.get<T>(name) ?: default) as T
        }
    }

    fun bool(name: String, default: Boolean? = null) = ConfigurationDelegate(name, default)
    fun integer(name: String, default: Int? = null) = ConfigurationDelegate(name, default)
    fun string(name: String, default: String? = null) = ConfigurationDelegate(name, default)
    fun <T> list(name: String, default: List<T>? = null) = ConfigurationDelegate(name, default)

    abstract fun <T> get(key: String): T?

    companion object {
        fun from(file: File) = ConfigurationYAML(file)
    }
}
