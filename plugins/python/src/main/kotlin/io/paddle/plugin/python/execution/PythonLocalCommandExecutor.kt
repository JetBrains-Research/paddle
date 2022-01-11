package io.paddle.plugin.python.execution

import io.paddle.execution.ExecutionResult
import io.paddle.execution.local.LocalCommandExecutor
import io.paddle.terminal.Terminal
import io.paddle.terminal.TextOutput
import org.codehaus.plexus.util.Os
import java.io.File

class PythonLocalCommandExecutor(output: TextOutput) : LocalCommandExecutor(output) {
    override fun execute(
        command: String,
        args: Iterable<String>,
        workingDir: File,
        terminal: Terminal,
        envVars: Map<String, String>,
        verbose: Boolean
    ): ExecutionResult {
        val newEnvVars = envVars.toMutableMap()

        if (Os.isFamily(Os.FAMILY_MAC)) {
            newEnvVars["LDFLAGS"] = (newEnvVars["LDFLAGS"] ?: "") + listOf(
                "-L/usr/local/opt/sqlite/lib",
                "-L/usr/local/opt/zlib/lib",
                "-L/usr/local/opt/readline/lib",
                "-L/usr/local/opt/openssl@3/lib"
            ).joinToString(" ")
            newEnvVars["CPPFLAGS"] = (newEnvVars["CPPFLAGS"] ?: "") + listOf(
                "-I/usr/local/opt/sqlite/include",
                "-I/usr/local/opt/zlib/include",
                "-I/usr/local/opt/readline/include",
                "-I/usr/local/opt/openssl@3/include"
            ).joinToString(" ")
        }

        return super.execute(command, args, workingDir, terminal, newEnvVars, verbose)
    }
}
