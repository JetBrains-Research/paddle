package io.paddle.plugin.python.dependencies.interpretator

import io.paddle.plugin.python.utils.RegexCache
import io.paddle.project.PaddleProject
import java.io.File
import kotlin.io.path.Path

internal class DockerTestInterpreterDownloader(private val userDefinedVersion: InterpreterVersion, private val project: PaddleProject) :
    UnixInterpreterDownloader(userDefinedVersion, project) {

    init {
        project.terminal.debug("Running Docker interpreter downloader. If you are not running tests, this is a bug")
    }

    override fun findLocalInstallation(): PyInterpreter? {
        var bestCandidate: PyInterpreter? = null
        lateinit var path: String
        project.executor.execute(
            "printenv",
            args = listOf("PATH"),
            terminal = project.terminal,
            workingDir = project.workDir,
            systemOut = { path = it },
            verbose = true
        )
        path.split(":").forEach { pathElem ->
            val filesInPath = mutableListOf<String>()
            project.executor.execute(
                "ls",
                args = listOf("-1", "--color=never", pathElem),
                terminal = project.terminal,
                workingDir = project.workDir,
                systemOut = { filesInPath.add(it) },
                verbose = true
            )
            filesInPath.filter { it.matches(RegexCache.PYTHON_EXECUTABLE_REGEX) }
                .forEach { execFile ->
                    val currentVersion = PyInterpreter.getVersion(File(pathElem, execFile))
                    if (currentVersion.matches(userDefinedVersion)) {
                        if (bestCandidate == null || bestCandidate!!.version <= currentVersion) {
                            bestCandidate = PyInterpreter(Path(pathElem, execFile), currentVersion)
                        }
                    }
                }
        }
        return bestCandidate?.also {
            project.terminal.info("Found local installation of ${it.version.fullName}: ${it.path}")
        }
    }
}
