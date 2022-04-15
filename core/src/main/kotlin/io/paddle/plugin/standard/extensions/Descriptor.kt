package io.paddle.plugin.standard.extensions

import io.paddle.project.PaddleProject
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable

val PaddleProject.descriptor: Descriptor
    get() = extensions.get(Descriptor.Extension.key)!!

class Descriptor(
    val name: String,
    val version: String,
    val author: String?,
    val authorEmail: String?,
    val description: String?,
    val url: String?
) {
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
            }

            return Descriptor(
                name = config.name,
                version = config.version,
                author = config.author,
                authorEmail = config.authorEmail,
                description = config.description,
                url = config.url
            )
        }
    }

}
