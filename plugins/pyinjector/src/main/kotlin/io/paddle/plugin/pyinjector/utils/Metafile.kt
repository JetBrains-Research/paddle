package io.paddle.plugin.pyinjector.utils

import io.paddle.plugin.pyinjector.dependencies.PyModule
import io.paddle.utils.config.ConfigurationView
import java.io.File

object Metafile {
    /**
     * Parse a file with meta information about specified local plugins' repository.
     *
     * @param file  A file with information
     * @throws IllegalArgumentException If file has no necessary information
     *
     * File format:
     *      plugins:
     *          - name: [plugin-name-1]
     *            module: [filename-1 or path]
     *
     *          - name: [plugin-name-2]
     *            module: [filename-2 or path]
     */
    fun parse(file: File): List<PyModule.Description> {
        val config = object : ConfigurationView("", from(file)) {
            val plugins: List<Map<String, String>> by list("plugins", emptyList())
        }
        return config.plugins.map {
            val name = it["name"]
            val module = it["module"]
            requireNotNull(name)
            requireNotNull(module)

            PyModule.Description(name, module)
        }
    }
}
