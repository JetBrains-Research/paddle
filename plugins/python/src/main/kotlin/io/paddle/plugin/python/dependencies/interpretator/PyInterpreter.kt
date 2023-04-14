package io.paddle.plugin.python.dependencies.interpretator

import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import java.io.File
import java.nio.file.Path

// @path is a path to executable in ./paddle/interpreters/... OR some local installation, not the local project/.venv/bin/python
// (but the last one is a symlink to it)
class PyInterpreter(val path: Path, val version: InterpreterVersion) {
    companion object {
        fun find(userDefinedVersion: InterpreterVersion, project: PaddleProject): PyInterpreter {
            return findCachedInstallation(userDefinedVersion, project)
                ?: findLocalInstallation(userDefinedVersion, project)
                ?: downloadAndInstall(userDefinedVersion, project)
        }

        private fun downloadAndInstall(userDefinedVersion: InterpreterVersion, project: PaddleProject): PyInterpreter =
            AbstractInterpreterDownloader.getDownloader(project).downloadAndInstall(userDefinedVersion)

        private fun findCachedInstallation(userDefinedVersion: InterpreterVersion, project: PaddleProject): PyInterpreter? =
            AbstractInterpreterDownloader
                .getDownloader(project)
                .findCachedInstallation()
                .find { it.version.matches(userDefinedVersion)  }
                ?.also { project.terminal.info("Found cached installation of ${it.version.fullName}: ${it.path}") }

        private fun findLocalInstallation(userDefinedVersion: InterpreterVersion, project: PaddleProject): PyInterpreter? =
            AbstractInterpreterDownloader
                .getDownloader(project)
                .findLocalInstallation()
                .asSequence()
                .filter { it.version.matches(userDefinedVersion)  }
                .maxByOrNull { it.version }
                ?.also { project.terminal.info("Found local installation of ${it.version.fullName}: ${it.path}") }

        fun getVersion(pythonExecutable: File, project: PaddleProject): InterpreterVersion {
            var currentVersionNumber: String? = null
            val processOutput = { line: String ->
                currentVersionNumber = line.substringAfter("Python ", "").takeIf { it != "" }
            }
            project.executor.execute(
                pythonExecutable.absolutePath,
                args = listOf("--version"),
                workingDir = project.workDir,
                terminal = project.terminal,
                systemOut = processOutput,
                systemErr = processOutput,
                verbose = false
            )
            return currentVersionNumber?.let { InterpreterVersion(it) }
                ?: throw Task.ActException("Failed to determine version for interpreter: ${pythonExecutable.absolutePath}")
        }
    }
}
