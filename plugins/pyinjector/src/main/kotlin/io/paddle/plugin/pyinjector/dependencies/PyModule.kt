package io.paddle.plugin.pyinjector.dependencies

import java.nio.file.Path

typealias PyModuleName = String

data class PyModule(val name: PyModuleName, val absolutePathTo: Path) {
    data class Description(val name: String, val filenameOrPath: String)
}
