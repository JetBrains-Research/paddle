package io.paddle.project.extensions

import io.paddle.project.PaddleProject
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable

val PaddleProject.descriptor: Descriptor
    get() = extensions.get(Descriptor.Extension.key)!!

class Descriptor(
    val name: String,
    val version: String,
    val author: String?,
    val authorEmail: String?,
    val description: String?,
    val url: String?,
    val classifiers: List<String>?
) : Hashable {
    object Extension : PaddleProject.Extension<Descriptor> {
        override val key: Extendable.Key<Descriptor> = Extendable.Key()

        override fun create(project: PaddleProject): Descriptor {
            val config = object : ConfigurationView("descriptor", project.config) {
                val name: String by string("name")
                val version: String by string("version")
                val author: String? by string("author")
                val authorEmail: String? by string("author_email")
                val description: String? by string("description")
                val url: String? by string("url")
                val classifiers: List<String>? by list("classifiers")
            }

            return Descriptor(
                name = config.name,
                version = config.version,
                author = config.author,
                authorEmail = config.authorEmail,
                description = config.description,
                url = config.url,
                classifiers = config.classifiers
            )
        }
    }

    override fun hash(): String {
        return (listOf(name, version, author, authorEmail, description, url) + classifiers).map { it.toString().hashable() }.hashable().hash()
    }
}
