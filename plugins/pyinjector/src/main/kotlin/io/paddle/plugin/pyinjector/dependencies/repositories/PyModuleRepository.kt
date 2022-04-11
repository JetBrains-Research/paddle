package io.paddle.plugin.pyinjector.dependencies.repositories

import io.paddle.plugin.pyinjector.dependencies.PyModule
import io.paddle.plugin.pyinjector.dependencies.PyModuleName
import java.nio.file.Path

typealias PyModuleRepoName = String

class PyModuleRepository(val name: PyModuleRepoName, val absolutePathTo: Path, private val modules /* TODO: add effective data structure to get all by names*/: Map<PyModuleName, PyModule>) {
    operator fun get(moduleName: PyModuleName): PyModule? = modules[moduleName]
}
