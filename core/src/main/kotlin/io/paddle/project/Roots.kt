package io.paddle.project

import io.paddle.utils.config.Configuration
import io.paddle.utils.config.ConfigurationView
import java.io.File

class Roots(val sources: List<File>, val tests: List<File>) {
    companion object {
        fun from(configuration: Configuration): Roots {
            val view = ConfigurationView("roots", configuration)
            val sources = view.get<List<String>>("sources") ?: emptyList()
            val tests = view.get<List<String>>("tests") ?: emptyList()
            return Roots(sources = sources.map { File(it) }, tests = tests.map { File(it) })
        }
    }
}
