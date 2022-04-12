package io.paddle.plugin.standard.extensions

import io.paddle.project.PaddleProject
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import java.io.File

val PaddleProject.roots: Roots
    get() = extensions.get(Roots.Extension.key)!!

class Roots(val sources: MutableList<File>, val tests: List<File>, val resources: List<File>) {
    object Extension : PaddleProject.Extension<Roots> {
        override val key: Extendable.Key<Roots> = Extendable.Key()

        override fun create(project: PaddleProject): Roots {
            val config = object : ConfigurationView("roots", project.config) {
                val sources by list("sources", default = listOf("src"))
                val tests by list("tests", default = listOf("tests"))
                val resources by list("resources", default = listOf("resources"))
            }

            return Roots(
                sources = config.sources.map { File(project.workDir, it) }.toMutableList(),
                tests = config.tests.map { File(project.workDir, it) }.toMutableList(),
                resources = config.resources.map { File(project.workDir, it) }.toMutableList()
            )
        }
    }

}
