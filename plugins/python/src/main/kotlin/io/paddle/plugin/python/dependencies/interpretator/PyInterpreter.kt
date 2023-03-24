package io.paddle.plugin.python.dependencies.interpretator

import io.paddle.plugin.python.extensions.pyLocations
import io.paddle.plugin.python.utils.deepResolve
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import org.codehaus.plexus.util.cli.CommandLineUtils
import org.codehaus.plexus.util.cli.Commandline
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
            AbstractInterpreterDownloader.getDownloader(userDefinedVersion, project).downloadAndInstall()

        private fun findCachedInstallation(userDefinedVersion: InterpreterVersion, project: PaddleProject): PyInterpreter? {
            val interpreterDir = project.pyLocations.interpretersDir.toFile().listFiles()
                ?.filter { it.isDirectory && InterpreterVersion(it.name).matches(userDefinedVersion) }
                ?.maxByOrNull { InterpreterVersion(it.name) }
            val execFile = interpreterDir?.deepResolve(
                "Python-${interpreterDir.name}",
                PythonPaths.LOCAL_PYTHON_DIR_NAME,
                "bin",
                userDefinedVersion.executableName
            )
            return execFile?.takeIf { it.exists() }?.let { PyInterpreter(it.toPath(), getVersion(it)) }?.also {
                project.terminal.info("Found cached installation of ${it.version.fullName}: ${it.path}")
            }
        }

        private fun findLocalInstallation(userDefinedVersion: InterpreterVersion, project: PaddleProject) =
            AbstractInterpreterDownloader.getDownloader(userDefinedVersion, project).findLocalInstallation()

        fun getVersion(pythonExecutable: File): InterpreterVersion {
            var currentVersionNumber: String? = null
            val processOutput = { line: String ->
                currentVersionNumber = line.substringAfter("Python ", "").takeIf { it != "" }
            }
            CommandLineUtils.executeCommandLine(
                Commandline().apply {
                    executable = pythonExecutable.absolutePath
                    addArguments(listOf("--version").toTypedArray())
                },
                processOutput,
                processOutput
            )
            return currentVersionNumber?.let { InterpreterVersion(it) }
                ?: throw Task.ActException("Failed to determine version for interpreter: ${pythonExecutable.absolutePath}")
        }
    }
}
