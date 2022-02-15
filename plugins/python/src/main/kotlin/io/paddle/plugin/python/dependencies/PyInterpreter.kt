package io.paddle.plugin.python.dependencies

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
import org.codehaus.plexus.util.Os
import java.io.File
import java.nio.file.Path

// @path is a path to ./paddle/interpreters/... , not the local project/.venv/bin/python
// (but the last one is a symlink for it)
class PyInterpreter(val path: Path, val version: Version) {
    companion object {
        private const val PYTHON_DISTRIBUTIONS_BASE_URL = "http://www.python.org/ftp/python/"
        private const val LOCAL_PYTHON_DIR_NAME = ".localpython"

        fun find(version: Version, project: Project): PyInterpreter {
            val loc = getLocation(version, project)
            return PyInterpreter(
                path = loc.takeIf { it.exists() } ?: downloadAndInstall(version, project),
                version = version
            )
        }

        fun getLocation(version: Version, project: Project): Path {
            // todo: check local installations ...
            return PaddlePyConfig.interpretersDir.deepResolve(
                version.number,
                version.fullName,
                LOCAL_PYTHON_DIR_NAME,
                "bin",
                version.executableName
            )
        }

        // TODO: implement layers caching
        private fun downloadAndInstall(version: Version, project: Project): Path {
            project.terminal.info("Downloading interpreter ${version.fullName}...")
            val workDir = PaddlePyConfig.interpretersDir.resolve(version.number).toFile().also { it.mkdirs() }

            val pythonDistName = "Python-${version}"
            val archiveDistName = "$pythonDistName.tgz"
            val url = PYTHON_DISTRIBUTIONS_BASE_URL.join(version.number, archiveDistName).trimEnd('/')
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

            // TODO: support Win?
            project.terminal.info("Installing interpreter...")
            val localPythonDir = extractDir.resolve(LOCAL_PYTHON_DIR_NAME).also { it.mkdirs() }
            val repoDir = extractDir.resolve(pythonDistName)

            val configureArgs =
                if (Os.isFamily(Os.FAMILY_MAC)) {
                    listOf(
                        "--prefix=${localPythonDir.absolutePath}",
                        "--with-openssl=/usr/local/opt/openssl"
                    )
                } else if (Os.isFamily(Os.FAMILY_UNIX)) {
                    listOf(
                        "--prefix=${localPythonDir.absolutePath}",
                        "--with-openssl=/usr/local/ssl"
                    )
                } else {
                    throw NotImplementedError("Windows is not supported yet.")
                }

            val envVars = if (Os.isFamily(Os.FAMILY_MAC)) {
                hashMapOf(
                    "LDFLAGS" to listOf(
                        "-L/usr/local/opt/sqlite/lib",
                        "-L/usr/local/opt/zlib/lib",
                        "-L/usr/local/opt/readline/lib",
                        "-L/usr/local/opt/openssl@3/lib"
                    ).joinToString(" "),
                    "CPPFLAGS" to listOf(
                        "-I/usr/local/opt/sqlite/include",
                        "-I/usr/local/opt/zlib/include",
                        "-I/usr/local/opt/readline/include",
                        "-I/usr/local/opt/openssl@3/include"
                    ).joinToString(" ")
                )
            } else {
                emptyMap()
            }

            project.executor.run {
                execute("./configure", configureArgs, repoDir, envVars = envVars, terminal = project.terminal)
                    .then {
                        execute("make", emptyList(), repoDir, envVars = envVars, terminal = project.terminal)
                    }.then {
                        execute("make", listOf("install"), repoDir, envVars = envVars, terminal = project.terminal)
                    }.orElseDo { code ->
                        error("Failed to install interpreter $pythonDistName. Exit code is $code")
                    }
            }
            project.terminal.info("Interpreter installed to ${localPythonDir.resolve("bin").path}")

            return localPythonDir.deepResolve("bin", version.executableName).toPath()
        }

        private fun downloadArchive(url: String, target: File, project: Project) = runBlocking {
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
                        target.appendBytes(bytes)
                        if (target.length() % 1000 == 0L) {
                            project.terminal.info("Received ${target.length()} bytes from ${httpResponse.contentLength()}")
                        }
                    }
                }
                project.terminal.info("Interpreter $url downloaded to ${target.path}")
            }
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

    data class Version(val number: String) {
        init {
            require(number.matches(RegexCache.PYTHON_VERSION_REGEX)) { "Invalid python version specified." }
        }

        private val supportedImplementations = listOf("cp", "py")

        val pep425candidates: List<String>
            get() {
                val parts = number.split(".")
                val major = parts[0].toInt()
                val minor = parts.getOrNull(1)?.toInt() ?: return supportedImplementations.map { it + major }
                return (minor downTo 0).toList()
                    .product(supportedImplementations)
                    .map { (minor, impl) -> "$impl$major$minor" } +
                    supportedImplementations.map { it + major }
            }

        val executableName: String
            get() = "python${number.first()}"

        val fullName: String
            get() = "Python-$number"

        override fun toString() = number
    }
}
