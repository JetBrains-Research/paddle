package io.paddle.plugin.standard.extensions

import io.paddle.project.PaddleProject
import io.paddle.utils.config.*
import io.paddle.utils.ext.Extendable
import kotlin.io.path.readText

val PaddleProject.registry: Registry
    get() = extensions.getOrFail(Registry.Extension.key)

class Registry(val project: PaddleProject) : Configuration() {
    val showStackTrace by lazy {
        get<String>("showStackTrace")?.toBoolean() ?: false
    }
    object Extension : PaddleProject.Extension<Registry> {
        override val key: Extendable.Key<Registry> = Extendable.Key()

        override fun create(project: PaddleProject) = Registry(project)
    }

    private val registryConfig: ConfigurationChain
        get() = project.locations.registry
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
