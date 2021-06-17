package io.paddle.project

import io.paddle.project.config.Configuration
import java.io.File

class Roots(val sources: List<File>, val tests: List<File>) {
    companion object {
        fun from(configuration: Configuration): Roots {
            val roots = configuration.roots
            return Roots(sources = roots.sources.map { File(it) }, tests = roots.tests.map { File(it) })
        }
    }
}
