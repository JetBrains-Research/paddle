package io.paddle.plugin.python.dependencies.interpreter

import io.paddle.plugin.python.utils.RegexCache
import io.paddle.project.PaddleProject
import java.io.File
import kotlin.io.path.Path

internal class DockerTestInterpreterDownloader(private val project: PaddleProject) :
    UnixInterpreterDownloader(project) {

    init {
        project.terminal.debug("Running Docker interpreter downloader. If you are not running tests, this is a bug")
    }

    override fun findLocalInstallation(): Collection<PyInterpreter> {
        lateinit var path: String
        project.executor.execute(
            "printenv",
            args = listOf("PATH"),
            terminal = project.terminal,
            workingDir = project.workDir,
            systemOut = { line -> line.takeIf { it.isNotBlank() }?.let { path = it } },
            verbose = true
        )
        return path.split(":").flatMap { pythonExec ->
            val filesInPath = mutableListOf<String>()
            project.executor.execute(
                "ls",
                args = listOf("-1", "--color=never", pythonExec),
                terminal = project.terminal,
                workingDir = project.workDir,
                systemOut = { filesInPath.add(it) },
                verbose = true
            )
            filesInPath
                .filter { it.matches(RegexCache.PYTHON_EXECUTABLE_REGEX) }
                .map { execFile ->
                    val version = PyInterpreter.getVersion(File(pythonExec, execFile), project)
                    PyInterpreter(Path(pythonExec, execFile), version)
                }
        }
    }
}
