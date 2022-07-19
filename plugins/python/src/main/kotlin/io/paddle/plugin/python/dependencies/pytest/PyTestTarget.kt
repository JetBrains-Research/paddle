package io.paddle.plugin.python.dependencies.pytest

import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.PaddleProject
import java.io.File

data class PyTestTarget(val cliArgument: String, val pycharmArgument: String, val type: Type) {
    enum class Type {
        FILE,
        DIRECTORY,
        NODE_ID
    }

    companion object {
        fun from(string: String, project: PaddleProject): PyTestTarget {
            val testRoot = project.roots.tests
            val relativePath = testRoot.relativeTo(project.workDir).toString()
            when {
                string.contains("::") -> {
                    val parts = string.split("::")
                    val moduleFilename = parts.firstOrNull() ?: throw PyTestTargetParseException("Invalid node id: $string")
                    val moduleName = moduleFilename.replace(File.separatorChar, '.')
                    val nodeName = parts.drop(1).joinToString(".")

                    val cliArgument = relativePath + File.separator + string
                    val pycharmArgument = "${relativePath.replace(File.separatorChar, '.')}.${moduleName.dropLast(3)}.$nodeName"

                    return PyTestTarget(cliArgument, pycharmArgument, Type.NODE_ID)
                }

                string.endsWith(".py") -> {
                    testRoot.resolve(string).takeIf { it.exists() } ?: throw PyTestTargetParseException("File not found: $string")
                    val filePath = relativePath + File.separator + string
                    return PyTestTarget(filePath, filePath, Type.FILE)
                }

                else -> {
                    testRoot.resolve(string).takeIf { it.exists() } ?: throw PyTestTargetParseException("Directory not found: $string")
                    val directoryPath = relativePath + File.separator + string
                    return PyTestTarget(directoryPath, directoryPath, Type.DIRECTORY)
                }
            }
        }
    }

    class PyTestTargetParseException(reason: String) : Exception(reason)
}
