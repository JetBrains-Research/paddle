package io.paddle.plugin.repository

object StandardPluginsRepository : AbstractPluginsRepository() {
    override val configPath: String
        get() = "META-INF/standard-plugins.yaml"
    override val classLoader: ClassLoader
        get() = this.javaClass.classLoader
}
