package io.paddle.plugin.pyinjector.dependencies.repositories

import io.paddle.plugin.pyinjector.dependencies.PyModule
import io.paddle.plugin.pyinjector.dependencies.PyModuleName
import java.nio.file.Path

typealias PyModuleRepoName = String

class PyModuleRepository(val name: PyModuleRepoName, val absolutePathTo: Path, private val modules: Map<PyModuleName, PyModule>) {
    val availableModulesNames: Set<PyModuleName>
        get() = modules.keys

    operator fun get(moduleName: PyModuleName): PyModule? = modules[moduleName]

}
