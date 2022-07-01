package io.paddle.plugin.standard.extensions

import io.paddle.project.PaddleProject
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable

val PaddleProject.descriptor: Descriptor
    get() = extensions.get(Descriptor.Extension.key)!!

class Descriptor(val name: String, val version: String) {
    object Extension: PaddleProject.Extension<Descriptor> {
        override val key: Extendable.Key<Descriptor> = Extendable.Key()

        override fun create(project: PaddleProject): Descriptor {
            val config = object: ConfigurationView("descriptor", project.config) {
                val name: String by string("name")
                val version: String by string("version")
            }

            return Descriptor(
                name = config.name,
                version = config.version
            )
        }
    }

}
