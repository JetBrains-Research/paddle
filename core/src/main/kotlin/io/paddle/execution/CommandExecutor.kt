package io.paddle.execution

import io.paddle.terminal.Terminal
import org.codehaus.plexus.util.Os
import java.io.File
import java.util.function.Consumer

interface CommandExecutor {
    val os: OsInfo

    val env: EnvProvider

    val runningProcesses: MutableSet<Process>

    fun execute(
        command: String,
        args: Iterable<String>,
        workingDir: File,
        terminal: Terminal,
        env: Map<String, String> = emptyMap(),
        verbose: Boolean = true,
        systemOut: Consumer<String> = Consumer { terminal.stdout(it) },
        systemErr: Consumer<String> = Consumer { terminal.stderr(it) }
    ): ExecutionResult

    interface OsInfo {
        val name: String
        val arch: String
        val userHome: String

        val familyPep425: String
            get() = when {
                name.lowercase() == Os.FAMILY_WINDOWS -> "win"
                name.lowercase() == Os.FAMILY_MAC -> "mac"
                name.lowercase() == Os.FAMILY_UNIX -> "linux"
                else -> error("Unknown OS family.")
            }

        val archPep425: String
            get() = when {
                "86" in arch && "64" in arch -> "x86_64"
                "64" in arch && ("arm" in arch || "aarch" in arch) -> "arm64"
                "64" in arch && "amd" in arch -> "amd64"
                "32" in arch -> "32"
                "86" in arch -> "86"
                else -> error("Unknown OS architecture.")
            }
    }
}
