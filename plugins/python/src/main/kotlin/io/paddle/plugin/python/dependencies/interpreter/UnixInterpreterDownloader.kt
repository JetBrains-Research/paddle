package io.paddle.plugin.python.dependencies.interpreter

import io.paddle.project.PaddleProject
import java.io.File

internal open class UnixInterpreterDownloader(project: PaddleProject) :
    AbstractInterpreterDownloader(project) {

    override fun getConfigureArgs(extractDir: File): List<String> {
        val localPythonDir = extractDir.resolve(PythonPaths.LOCAL_PYTHON_DIR_NAME).also { it.mkdirs() }
        return listOf("--prefix=${localPythonDir.absolutePath}")
    }
}
