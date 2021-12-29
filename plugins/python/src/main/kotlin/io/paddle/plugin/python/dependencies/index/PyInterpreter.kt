package io.paddle.plugin.python.dependencies.index

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.paddle.plugin.python.PaddlePyConfig
import io.paddle.plugin.python.utils.*
import io.paddle.project.Project
import kotlinx.coroutines.runBlocking
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver
import org.codehaus.plexus.logging.console.ConsoleLoggerManager
import java.io.File
import java.nio.file.Path


class PyInterpreter(val path: Path, val version: Version) {
    companion object {
        private const val PYTHON_DISTRIBUTIONS_BASE_URL = "http://www.python.org/ftp/python/"
        private const val LOCAL_PYTHON_DIR_NAME = ".localpython"

        fun find(version: Version, project: Project): PyInterpreter {
            val interpreterPath = PaddlePyConfig.interpreters.deepResolve(
                version.src,
                version.fullName,
                LOCAL_PYTHON_DIR_NAME,
                "bin",
                version.executableName
            )
            return PyInterpreter(
                path = interpreterPath.takeIf { it.exists() } ?: downloadAndInstall(version, project),
                version = version
            )
        }

        // TODO: implement layers caching
        private fun downloadAndInstall(version: Version, project: Project): Path {
            val workDir = PaddlePyConfig.interpreters.resolve(version.src).toFile().also { it.mkdirs() }

            val pythonDistName = "Python-${version}"
            val archiveDistName = "$pythonDistName.tgz"
            val url = PYTHON_DISTRIBUTIONS_BASE_URL.join(version.src, archiveDistName).trimEnd('/')
            val file = workDir.resolve(archiveDistName)
            runBlocking {
                httpClient.get<HttpStatement>(url).execute { httpResponse ->
                    when {
                        httpResponse.status == HttpStatusCode.NotFound -> error("The specified interpreter was not found at $url: ${httpResponse.status}")
                        httpResponse.status != HttpStatusCode.OK -> error("Problems with network access: $url, status: $httpResponse.status")
                    }
                    val channel: ByteReadChannel = httpResponse.receive()
                    while (!channel.isClosedForRead) {
                        val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                        while (!packet.isEmpty) {
                            val bytes = packet.readBytes()
                            file.appendBytes(bytes)
                            if (file.length() % 1000 == 0L) {
                                println("Received ${file.length()} bytes from ${httpResponse.contentLength()}")
                            }
                        }
                    }
                    println("$archiveDistName saved to ${file.path}")
                }
            }

            val extractDir = workDir.resolve(pythonDistName).also { it.mkdirs() }
            unpackTarGZip(workDir.resolve(archiveDistName), extractDir)

            // TODO: support Win
            val localPythonDir = extractDir.resolve(LOCAL_PYTHON_DIR_NAME).also { it.mkdirs() }
            val repoDir = extractDir.resolve(pythonDistName)
            project.executor.run {
                execute("./configure", listOf("--prefix=${localPythonDir.absolutePath}"), repoDir, project.terminal)
                    .then {
                        execute("make", emptyList(), repoDir, project.terminal)
                    }.then {
                        execute("make", listOf("install"), repoDir, project.terminal)
                    }.orElseDo { code ->
                        error("Failed to install $pythonDistName. Exit code is $code")
                    }
            }

            return localPythonDir.deepResolve("bin", version.executableName).toPath()
        }

        private fun unpackTarGZip(sourceFile: File, destDirectory: File) {
            TarGZipUnArchiver().run {
                val consoleLoggerManager = ConsoleLoggerManager().also { it.initialize() }
                enableLogging(consoleLoggerManager.getLoggerForComponent("python-tgz-un-archiver"))
                this.sourceFile = sourceFile
                this.destDirectory = destDirectory
                extract()
            }
        }
    }

    data class Version(val src: String) {
        init {
            require(src.matches(RegexCache.PYTHON_VERSION_REGEX)) { "Invalid python version specified." }
        }

        private val supportedImplementations = listOf("cp", "py")

        val pep425candidates: List<String>
            get() {
                val currentVersion = src.replace(".", "").toInt()
                val candidateVersions =
                    if (currentVersion > 20) {
                        val base = currentVersion.div(10)
                        (currentVersion downTo (base * 10)).toList() + base
                    } else {
                        listOf(currentVersion)
                    }
                return supportedImplementations.product(candidateVersions).map { "${it.first}${it.second}" }
            }

        val executableName: String
            get() = "python${src.first()}"

        val fullName: String
            get() = "Python-$src"

        override fun toString() = src
    }
}
