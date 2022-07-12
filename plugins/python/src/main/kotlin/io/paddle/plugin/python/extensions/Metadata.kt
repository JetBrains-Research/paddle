package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.utils.takeIfAllAreEqual
import io.paddle.project.PaddleProject
import io.paddle.project.extensions.routeAsString
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable

val PaddleProject.metadata: Metadata
    get() = extensions.get(Metadata.Extension.key)!!

class Metadata private constructor(val project: PaddleProject, private val config: MetadataConfigurationView) : Hashable {
    val version: String by lazy {
        config.version
            ?: project.parents.map { it.metadata.version }.takeIfAllAreEqual()?.firstOrNull()
            ?: error("Version <metadata.version> for project ${project.routeAsString} is not provided and could not be inferred.")
    }
    val author: String by lazy {
        config.author
            ?: project.parents.map { it.metadata.author }.takeIfAllAreEqual()?.firstOrNull()
            ?: error("Author <metadata.author> for project ${project.routeAsString} is not provided.")
    }
    val authorEmail: String? by lazy { config.authorEmail ?: project.parents.mapNotNull { it.metadata.authorEmail }.takeIfAllAreEqual()?.firstOrNull() }
    val url: String? by lazy { config.url ?: project.parents.mapNotNull { it.metadata.url }.takeIfAllAreEqual()?.firstOrNull() }
    val description: String = config.description ?: "Description is not provided."
    val classifiers: List<String>? = config.classifiers
    val keywords: String? = config.keywords

    object Extension : PaddleProject.Extension<Metadata> {
        override val key: Extendable.Key<Metadata> = Extendable.Key()

        override fun create(project: PaddleProject): Metadata {
            val config = MetadataConfigurationView(project)
            return Metadata(project, config)
        }
    }

    private class MetadataConfigurationView(project: PaddleProject) : ConfigurationView("metadata", project.config) {
        val version: String? by string("version")
        val author: String? by string("author")
        val authorEmail: String? by string("authorEmail")
        val description: String? by string("description")
        val url: String? by string("url")
        val keywords: String? by string("keywords")
        val classifiers: List<String>? by list("classifiers")
    }

    override fun hash(): String {
        return (listOf(version, author, authorEmail, description, url) + classifiers).map { it.toString().hashable() }.hashable().hash()
    }
}
