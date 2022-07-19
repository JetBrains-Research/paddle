package io.paddle.plugin.standard.extensions

import io.paddle.project.PaddleProject
import io.paddle.project.extensions.routeAsString
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import java.io.File

val PaddleProject.roots: Roots
    get() = checkNotNull(extensions.get(Roots.Extension.key)) { "Could not load extension Roots for project $routeAsString" }

/**
 * Project roots extension: sources, tests and resources.
 *
 * Note: it is supposed that each project has only one root of each type,
 * but inside these folders there could be any number of python packages.
 */
class Roots(val sources: File, val tests: File, val resources: File, val dist: File) {
    object Extension : PaddleProject.Extension<Roots> {
        override val key: Extendable.Key<Roots> = Extendable.Key()

        override fun create(project: PaddleProject): Roots {
            val config = object : ConfigurationView("roots", project.config) {
                val sources by string("sources", default = "src")
                val tests by string("tests", default = "tests")
                val resources by string("resources", default = "resources")
                val dist by string("dist", default = "dist")
            }

            return Roots(
                sources = File(project.workDir, config.sources),
                tests = File(project.workDir, config.tests),
                resources = File(project.workDir, config.resources),
                dist = File(project.workDir, config.dist)
            )
        }
    }

}
