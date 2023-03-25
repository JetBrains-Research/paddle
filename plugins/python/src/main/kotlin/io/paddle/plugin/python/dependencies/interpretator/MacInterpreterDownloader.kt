package io.paddle.plugin.python.dependencies.interpretator

import io.paddle.project.PaddleProject
import java.io.File

internal class MacInterpreterDownloader(userDefinedVersion: InterpreterVersion, project: PaddleProject) :
    AbstractInterpreterDownloader(userDefinedVersion, project) {
    override fun installPreRequirements(project: PaddleProject) {
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
    }

    override fun getConfigureArgs(extractDir: File): List<String> {
        val localPythonDir = extractDir.resolve(PythonPaths.LOCAL_PYTHON_DIR_NAME).also { it.mkdirs() }
        return listOf("--prefix=${localPythonDir.absolutePath}")
    }

    override fun getEnvVars(extractDir: File): Map<String, String> = hashMapOf(
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
}
