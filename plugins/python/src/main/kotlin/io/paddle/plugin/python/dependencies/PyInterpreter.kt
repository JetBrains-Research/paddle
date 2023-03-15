package io.paddle.plugin.python.dependencies

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.paddle.plugin.python.extensions.pyLocations
import io.paddle.plugin.python.utils.*
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import kotlinx.coroutines.runBlocking
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver
import org.codehaus.plexus.util.Os
import org.codehaus.plexus.util.cli.CommandLineUtils
import org.codehaus.plexus.util.cli.Commandline
import org.jsoup.Jsoup
import java.io.File
import java.nio.file.Path

// @path is a path to executable in ./paddle/interpreters/... OR some local installation, not the local project/.venv/bin/python
// (but the last one is a symlink to it)
class PyInterpreter(val path: Path, val version: Version) {
    companion object {
        const val PYTHON_DISTRIBUTIONS_BASE_URL = "http://www.python.org/ftp/python/"
        private const val LOCAL_PYTHON_DIR_NAME = ".localpython"

        fun find(userDefinedVersion: Version, project: PaddleProject): PyInterpreter {
            return findCachedInstallation(userDefinedVersion, project)
                ?: findLocalInstallation(userDefinedVersion, project)
                ?: downloadAndInstall(userDefinedVersion, project)
        }

        private fun findCachedInstallation(userDefinedVersion: Version, project: PaddleProject): PyInterpreter? {
            val interpreterDir = project.pyLocations.interpretersDir.toFile().listFiles()
                ?.filter { it.isDirectory && Version(it.name).matches(userDefinedVersion) }
                ?.maxByOrNull { Version(it.name) }
            val execFile = interpreterDir?.deepResolve(
                "Python-${interpreterDir.name}",
                LOCAL_PYTHON_DIR_NAME,
                "bin",
                userDefinedVersion.executableName
            )
            return execFile?.takeIf { it.exists() }?.let { PyInterpreter(it.toPath(), getVersion(it)) }?.also {
                project.terminal.info("Found cached installation of ${it.version.fullName}: ${it.path}")
            }
        }

        private fun findLocalInstallation(userDefinedVersion: Version, project: PaddleProject): PyInterpreter? {
            if (Os.isFamily(Os.FAMILY_MAC) || Os.isFamily(Os.FAMILY_UNIX)) {
                var bestCandidate: PyInterpreter? = null
                System.getenv("PATH").split(":").forEach { path ->
                    File(path).listFiles()?.filter { it.name.matches(RegexCache.PYTHON_EXECUTABLE_REGEX) }
                        ?.forEach { execFile ->
                            val currentVersion = getVersion(execFile)
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
            } else {
                TODO("Windows is not supported yet.")
            }
        }

        fun getVersion(pythonExecutable: File): Version {
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
            return currentVersionNumber?.let { Version(it) }
                ?: throw Task.ActException("Failed to determine version for interpreter: ${pythonExecutable.absolutePath}")
        }

        // TODO: implement layers caching
        private fun downloadAndInstall(userDefinedVersion: Version, project: PaddleProject): PyInterpreter =
            runBlocking {
                val matchedVersion =
                    Version.getAvailableRemoteVersions().filter { it.matches(userDefinedVersion) }.maxOrNull()
                        ?: throw Task.ActException("Can't find an appropriate version at $PYTHON_DISTRIBUTIONS_BASE_URL for version $userDefinedVersion")

                project.terminal.info("Downloading interpreter ${matchedVersion.fullName}...")
                val workDir =
                    project.pyLocations.interpretersDir.resolve(matchedVersion.number).toFile().also { it.mkdirs() }

                val pythonDistName = "Python-${matchedVersion}"
                val archiveDistName = "$pythonDistName.tgz"
                val url = PYTHON_DISTRIBUTIONS_BASE_URL.join(matchedVersion.number, archiveDistName).trimEnd('/')
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

                tryInstallingPrerequisites(project)

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
                    execute("./configure", configureArgs, repoDir, env = envVars, terminal = project.terminal)
                        .then {
                            execute("make", emptyList(), repoDir, env = envVars, terminal = project.terminal)
                        }.then {
                            execute("make", listOf("install"), repoDir, env = envVars, terminal = project.terminal)
                        }.orElseDo { code ->
                            throw Task.ActException("Failed to install interpreter $pythonDistName. Exit code is $code")
                        }
                }
                project.terminal.info("Interpreter installed to ${localPythonDir.resolve("bin").path}")

                val path = localPythonDir.deepResolve("bin", matchedVersion.executableName).toPath()
                return@runBlocking PyInterpreter(path, matchedVersion)
            }

        private fun tryInstallingPrerequisites(project: PaddleProject) {
            if (Os.isFamily(Os.FAMILY_MAC)) {
                var macBrandString: String? = null
                project.executor.execute(
                    command = "sysctl",
                    args = listOf("-n", "machdep.cpu.brand_string"),
                    workingDir = project.rootDir,
                    terminal = project.terminal,
                    systemOut = { result -> macBrandString = result },
                    systemErr = { error -> project.terminal.warn("Failed to detect CPU type for your Mac: $error") }
                ).expose(
                    onSuccess = {
                        when {
                            macBrandString?.contains("M1") == true -> {
                                project.executor.execute(
                                    command = "arch",
                                    args = "-x86_64 brew install openssl gettext readline sqlite3 xz zlib tcl-tk".split(" "),
                                    workingDir = project.rootDir,
                                    terminal = project.terminal
                                )
                            }

                            else -> {
                                project.executor.execute(
                                    command = "/usr/local/bin/brew",
                                    args = "install openssl readline sqlite3 xz zlib tcl-tk".split(" "),
                                    workingDir = project.rootDir,
                                    terminal = project.terminal
                                )
                            }
                        }
                    },
                    onFail = {
                        macBrandString ?: run {
                            project.terminal.error("Failed to install prerequisites for Python: openssl readline sqlite3 xz zlib tcl-tk")
                        }
                    }
                )
            } else {
                // TODO()
            }
        }

        private fun downloadArchive(url: String, target: File, project: PaddleProject) = runBlocking {
            val httpResponse = httpClient.get(url)
            when {
                httpResponse.status == HttpStatusCode.NotFound ->
                    throw Task.ActException("The specified interpreter was not found at $url: ${httpResponse.status}")

                httpResponse.status != HttpStatusCode.OK ->
                    throw Task.ActException("Problems with network access: $url, status: $httpResponse.status")
            }
            val channel: ByteReadChannel = httpResponse.bodyAsChannel()
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

        private fun unpackTarGZip(sourceFile: File, destDirectory: File) {
            TarGZipUnArchiver().apply {
                this.sourceFile = sourceFile
                this.destDirectory = destDirectory
                extract()
            }
        }
    }

    data class Version(val number: String) : Comparable<Version> {
        init {
            require(number.matches(RegexCache.PYTHON_VERSION_REGEX) && number.count { it == '.' } <= 2) {
                "Invalid python version specified."
            }
        }

        companion object {
            suspend fun getAvailableRemoteVersions(): Collection<Version> {
                val httpResponse = httpClient.get(PYTHON_DISTRIBUTIONS_BASE_URL)
                val page = Jsoup.parse(httpResponse.bodyAsText())
                return page.body().getElementsByTag("a")
                    .map { it.text().trim('/') }
                    .filter { it.matches(RegexCache.PYTHON_VERSION_REGEX) }
                    .map { Version(it) }
                    .toSet()
            }

            val localVersions: Collection<Version>
                get() = when {
                    Os.isFamily(Os.FAMILY_MAC) || Os.isFamily(Os.FAMILY_UNIX) ->
                        System.getenv("PATH").split(":").flatMap { path ->
                            File(path).listFiles()
                                ?.filter { it.name.matches(RegexCache.PYTHON_EXECUTABLE_REGEX) }
                                ?.map { execFile -> getVersion(execFile) }
                                ?: emptyList()
                        }

                    else -> emptyList()
                }
        }

        private val parts = number.split(".")
        private val supportedImplementations = listOf("cp", "py")
        private val latest: Int = 9

        val major: Int = parts[0].toInt()
        val minor: Int? = parts.getOrNull(1)?.toInt()
        val patch: Int? = parts.getOrNull(2)?.toInt()

        val pep425candidates: List<String>
            get() {
                minor ?: return supportedImplementations.map { it + major }
                return (minor downTo 0).toList()
                    .product(supportedImplementations)
                    .map { (minor, impl) -> "$impl$major$minor" } +
                    supportedImplementations.map { it + major }
            }

        val executableName: String
            get() = "python${major}"

        val fullName: String
            get() = "Python-$number"

        override fun toString() = number

        fun matches(userDefinedVersion: Version): Boolean {
            return when (userDefinedVersion.number.count { it == '.' }) {
                0 -> major == userDefinedVersion.major
                1 -> number.startsWith(userDefinedVersion.number)
                    && userDefinedVersion.number.substringAfter('.') == number.substringAfter('.')
                    .substringBefore('.')

                2 -> number == userDefinedVersion.number
                else -> throw IllegalStateException("Invalid python version specified.")
            }
        }

        // Because of versions like "3.10.1" we can't just compare double representations of the strings
        // Also, minor and patch version are not always specified
        override fun compareTo(other: Version): Int {
            if (major == other.major) {
                if (minor == null || other.minor == null) return 0
                if (minor == other.minor) {
                    if (patch == null || other.patch == null) return 0
                    return patch - other.patch
                } else {
                    return minor - other.minor
                }
            } else {
                return major - other.major
            }
        }
    }
}
