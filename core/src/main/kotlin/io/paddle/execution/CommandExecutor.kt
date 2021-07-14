package io.paddle.execution

import io.paddle.terminal.TerminalUI
import io.paddle.terminal.CommandOutput
import java.io.File

abstract class CommandExecutor(val configuration: OutputConfiguration) {
    data class OutputConfiguration(val output: CommandOutput, val printStdOut: Boolean = true, val printStdErr: Boolean = true)

    abstract fun execute(command: String, args: Iterable<String>, working: File, terminal: TerminalUI): Int
}
