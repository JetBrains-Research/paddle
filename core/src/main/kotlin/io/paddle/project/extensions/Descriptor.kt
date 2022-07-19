package io.paddle.project.extensions

import io.paddle.project.PaddleProject
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable

val PaddleProject.descriptor: Descriptor
    get() = checkNotNull(extensions.get(Descriptor.Extension.key)) { "Could not load extension Descriptor for project $routeAsString" }

class Descriptor(val name: String) : Hashable {
    object Extension : PaddleProject.Extension<Descriptor> {
        override val key: Extendable.Key<Descriptor> = Extendable.Key()

        override fun create(project: PaddleProject): Descriptor {
            val name = project.config.get<String>("project") ?: error("Project name <project> is not specified in ${project.buildFile.absolutePath}")
            return Descriptor(name)
        }
    }

    override fun hash(): String {
        return name.hashable().hash()
    }
}
