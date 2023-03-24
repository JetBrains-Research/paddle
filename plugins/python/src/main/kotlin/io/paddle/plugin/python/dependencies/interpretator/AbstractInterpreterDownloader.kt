package io.paddle.plugin.python.dependencies.interpretator

import io.paddle.plugin.python.extensions.pyLocations
import io.paddle.plugin.python.utils.*
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import kotlinx.coroutines.runBlocking
import org.codehaus.plexus.util.Os
import java.io.File

internal sealed class AbstractInterpreterDownloader(private val userDefinedVersion: InterpreterVersion, private val project: PaddleProject) {
    fun downloadAndInstall(): PyInterpreter =
        runBlocking {
            val matchedVersion =
                InterpreterVersion.getAvailableRemoteVersions().filter { it.matches(userDefinedVersion) }.maxOrNull()
                    ?: throw Task.ActException("Can't find an appropriate version at ${PythonPaths.PYTHON_DISTRIBUTIONS_BASE_URL} for version $userDefinedVersion")

            project.terminal.info("Downloading interpreter ${matchedVersion.fullName}...")
            val workDir =
                project.pyLocations.interpretersDir.resolve(matchedVersion.number).toFile().also { it.mkdirs() }

            val pythonDistName = "Python-${matchedVersion}"
            val archiveDistName = "$pythonDistName.tgz"
            val url = PythonPaths.PYTHON_DISTRIBUTIONS_BASE_URL.join(matchedVersion.number, archiveDistName).trimEnd('/')
            val file = workDir.resolve(archiveDistName)

            if (file.exists()) {
                project.terminal.info("Found downloaded distribution archive: ${file.path}")
            } else {
                downloadArchive(url, file, project)
            }

            val extractDir = workDir.resolve(pythonDistName).also {
                if (it.exists()) {
                    it.deleteRecursively()
                }
                it.mkdirs()
            }
            project.terminal.info("Unpacking archive: ${workDir.resolve(archiveDistName)}...")
            unpackTarGZip(workDir.resolve(archiveDistName), extractDir)
            project.terminal.info("Unpacking finished")

            installPreRequirements(project)

            // TODO: support Win?
            project.terminal.info("Installing interpreter...")
            val localPythonDir = extractDir.resolve(PythonPaths.LOCAL_PYTHON_DIR_NAME).also { it.mkdirs() }
            val repoDir = extractDir.resolve(pythonDistName)

            project.executor.run {
                execute("./configure", getConfigureArgs(extractDir), repoDir, env = getEnvVars(extractDir), terminal = project.terminal)
                    .then {
                        execute("make", emptyList(), repoDir, env = getEnvVars(extractDir), terminal = project.terminal)
                    }.then {
                        execute("make", listOf("install"), repoDir, env = getEnvVars(extractDir), terminal = project.terminal)
                    }.orElseDo { code ->
                        throw Task.ActException("Failed to install interpreter $pythonDistName. Exit code is $code")
                    }
            }
            project.terminal.info("Interpreter installed to ${localPythonDir.resolve("bin").path}")

            val path = localPythonDir.deepResolve("bin", matchedVersion.executableName).toPath()
            return@runBlocking PyInterpreter(path, matchedVersion)
        }

    protected open fun installPreRequirements(project: PaddleProject) = Unit
    protected open fun getConfigureArgs(extractDir: File) = emptyList<String>()
    protected open fun getEnvVars(extractDir: File) = emptyMap<String, String>()

    open fun findLocalInstallation(): PyInterpreter? {
        var bestCandidate: PyInterpreter? = null
        System.getenv("PATH").split(":").forEach { path ->
            File(path).listFiles()?.filter { it.name.matches(RegexCache.PYTHON_EXECUTABLE_REGEX) }
                ?.forEach { execFile ->
                    val currentVersion = PyInterpreter.getVersion(execFile)
                    if (currentVersion.matches(userDefinedVersion)) {
                        if (bestCandidate == null || bestCandidate!!.version <= currentVersion) {
                            bestCandidate = PyInterpreter(execFile.toPath(), currentVersion)
                        }
                    }
                }
        }
        return bestCandidate?.also {
            project.terminal.info("Found local installation of ${it.version.fullName}: ${it.path}")
        }
    }

    companion object {
        fun getDownloader(userDefinedVersion: InterpreterVersion, project: PaddleProject) = when {
            Os.isFamily(Os.FAMILY_UNIX) -> UnixInterpreterDownloader(userDefinedVersion, project)
            Os.isFamily(Os.FAMILY_MAC) -> MacInterpreterDownloader(userDefinedVersion, project)
            else -> GenericInterpreterDownloader(userDefinedVersion, project) // try to install via generic downloader
        }
    }
}
