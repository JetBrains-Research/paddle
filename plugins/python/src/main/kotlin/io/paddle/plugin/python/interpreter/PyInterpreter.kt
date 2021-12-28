package io.paddle.plugin.python.interpreter

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.paddle.plugin.python.PaddlePyConfig
import io.paddle.plugin.python.utils.*
import io.paddle.project.Project
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver
import org.codehaus.plexus.logging.console.ConsoleLoggerManager
import java.io.File
import java.nio.file.Path


class PyInterpreter(val path: Path, val version: Version) {
    companion object {
        private const val PYTHON_DISTR_BASE_URL = "http://www.python.org/ftp/python/"

        suspend fun find(version: Version, project: Project): PyInterpreter {
            return if (PaddlePyConfig.interpreters.resolve(version.text).exists()) {
                PaddlePyConfig.interpreters.resolve(version.text).resolve("bin").resolve(version.interpreterName)
            } else {
                downloadAndInstall(version, project)
            }.let { PyInterpreter(it, version) }
        }

        private suspend fun downloadAndInstall(version: Version, project: Project): Path {
            val workDir = PaddlePyConfig.interpreters.resolve(version.text).toFile().also { it.mkdirs() }

            val pythonDistName = "Python-${version}"
            val archiveDistName = "$pythonDistName.tgz"
            val url = PYTHON_DISTR_BASE_URL.join(version.text, archiveDistName)
            val httpResponse: HttpResponse = httpClient.get(url)
            val responseBody: ByteArray = httpResponse.receive()
            workDir.resolve(archiveDistName).writeBytes(responseBody)

            val pyWorkDir = workDir.resolve(pythonDistName).also { it.mkdirs() }
            unpackTarGZip(workDir.resolve(archiveDistName), pyWorkDir)

            // TODO: test Linux & MacOS
            // TODO: add Win?
            val localPythonDir = pyWorkDir.resolve(".localpython").also { it.mkdirs() }
            project.executor.run {
                execute("./configure", listOf("--prefix=${localPythonDir.absolutePath}"), pyWorkDir, project.terminal)
                execute("make && make install", emptyList(), pyWorkDir, project.terminal)
            }

            val pyInterpreterPath = localPythonDir.resolve("bin").resolve(version.interpreterName).toPath()
            return pyInterpreterPath
        }

        private suspend fun unpackTarGZip(sourceFile: File, destDirectory: File) = coroutineScope {
            launch {
                TarGZipUnArchiver().run {
                    val consoleLoggerManager = ConsoleLoggerManager().also { it.initialize() }
                    enableLogging(consoleLoggerManager.getLoggerForComponent("python-tgz-un-archiver"))
                    this.sourceFile = sourceFile
                    this.destDirectory = destDirectory
                    extract()
                }
            }
        }
    }

    data class Version(val text: String) {
        init {
            require(text.matches(RegexCache.PYTHON_VERSION_REGEX)) { "Invalid python version specified." }
        }

        private val supportedImplementations = listOf("cp", "py")

        val pep425candidates: List<String>
            get() {
                val currentVersion = text.replace(".", "").toInt()
                val candidateVersions =
                    if (currentVersion > 20) {
                        val base = currentVersion.div(10)
                        (currentVersion downTo (base * 10)).toList() + base
                    } else {
                        listOf(currentVersion)
                    }
                return supportedImplementations.product(candidateVersions).map { "${it.first}${it.second}" }
            }

        val interpreterName: String
            get() = "python${text}"

        override fun toString() = text
    }
}
