package io.paddle.project

import io.paddle.utils.config.Configuration
import io.paddle.utils.ext.Extendable
import java.io.File

object RootsExtension: Project.Extension<Roots> {
    override val key: Extendable.Key<Roots> = Extendable.Key()

    override fun create(project: Project): Roots {
        return Roots.from(project.config)
    }
}

val Project.roots: Roots
    get() = extensions.get(RootsExtension.key)!!

class Roots(val sources: MutableList<File>, val tests: List<File>, val resources: List<File>) {
    companion object {
        fun from(configuration: Configuration): Roots {
            val sources = configuration.get<List<String>>("roots.sources") ?: emptyList()
            val tests = configuration.get<List<String>>("roots.tests") ?: emptyList()
            val resources = configuration.get<List<String>>("roots.resources") ?: emptyList()
            return Roots(
                sources = sources.map { File(it) }.toMutableList(),
                tests = tests.map { File(it) }.toMutableList(),
                resources = resources.map { File(it) }.toMutableList()
            )
        }
    }
}
