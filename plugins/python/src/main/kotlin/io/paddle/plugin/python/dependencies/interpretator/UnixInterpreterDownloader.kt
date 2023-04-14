package io.paddle.plugin.python.dependencies.interpretator

import io.paddle.project.PaddleProject
import java.io.File

internal class UnixInterpreterDownloader(project: PaddleProject) :
    AbstractInterpreterDownloader(project) {

    override fun getConfigureArgs(extractDir: File): List<String> {
        val localPythonDir = extractDir.resolve(PythonPaths.LOCAL_PYTHON_DIR_NAME).also { it.mkdirs() }
        return listOf("--prefix=${localPythonDir.absolutePath}")
    }
}
