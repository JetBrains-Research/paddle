package io.paddle.plugin.python.dependencies.interpreter

import io.paddle.plugin.python.extensions.pyLocations
import io.paddle.plugin.python.utils.*
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.utils.config.PaddleApplicationSettings
import kotlinx.coroutines.runBlocking
import org.codehaus.plexus.util.Os
import java.io.File

internal open class AbstractInterpreterDownloader(private val project: PaddleProject) {
    fun downloadAndInstall(userDefinedVersion: InterpreterVersion): PyInterpreter =
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

    open fun findCachedInstallation(): Collection<PyInterpreter> = project.pyLocations.interpretersDir.toFile().listFiles()
        ?.filter { it.isDirectory }
        ?.map {
            val version = InterpreterVersion(it.name)
            val execFile = it.deepResolve(
                "Python-${it.name}",
                PythonPaths.LOCAL_PYTHON_DIR_NAME,
                "bin",
                version.executableName
            )
            execFile to version
        }
        ?.map { (file, version) -> PyInterpreter(file.toPath(), version) }
        ?: emptyList()

    open fun findLocalInstallation(): Collection<PyInterpreter> {
        return System.getenv("PATH").split(":").flatMap { path ->
            File(path)
                .listFiles()
                ?.filter { it.name.matches(RegexCache.PYTHON_EXECUTABLE_REGEX) }
                ?.map { execFile ->
                    val version = PyInterpreter.getVersion(execFile, project)
                    PyInterpreter(execFile.toPath(), version)
                }
                ?: emptyList()
        }
    }

    companion object {
        fun getDownloader(project: PaddleProject) = when {
            PaddleApplicationSettings.isTests -> DockerTestInterpreterDownloader(project)
            Os.isFamily(Os.FAMILY_UNIX) -> UnixInterpreterDownloader(project)
            Os.isFamily(Os.FAMILY_MAC) -> MacInterpreterDownloader(project)
            PaddleApplicationSettings.isTests -> DockerTestInterpreterDownloader(project)
            else -> GenericInterpreterDownloader(project) // try to install via generic downloader
        }
    }
}
