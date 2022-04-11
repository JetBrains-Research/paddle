package io.paddle.plugin.repositories

import io.paddle.plugin.Plugin
import io.paddle.project.Project
import io.paddle.utils.config.PluginsConfig
import io.paddle.utils.ext.Extendable
import java.io.File

val Project.jarPluginsRepositories: JarPluginsRepositories
    get() = extensions.get(JarPluginsRepositories.Extension.key)!!

class JarPluginsRepositories(private val storage: Map<String, JarPluginsRepository>) {

    object Extension : Project.Extension<JarPluginsRepositories> {
        override val key: Extendable.Key<JarPluginsRepositories> = Extendable.Key()

        override fun create(project: Project): JarPluginsRepositories {
            val config = object : PluginsConfig(project) {
                val jarRepos by repositories(withAttrs = setOf("jar"))
            }
            val repos: List<JarPluginsRepository> = config.jarRepos.map {
                val name = it["name"]
                requireNotNull(name)
                val file = it["jar"]?.let { filename -> File(filename) }
                requireNotNull(file)
                require(file.exists())

                JarPluginsRepository(name, file)
            }

            return JarPluginsRepositories(repos.associateBy { it.name })
        }
    }

    operator fun get(repoName: String): JarPluginsRepository? = storage[repoName]

    operator fun get(repoName: String, pluginName: String): Plugin? = storage[repoName]?.plugin(pluginName)

    fun contains(repoName: String): Boolean = storage.containsKey(repoName)

    fun findAvailablePluginsBy(prefix: String): Map<String, JarPluginsRepository> {
        TODO("implement")
    }
}
