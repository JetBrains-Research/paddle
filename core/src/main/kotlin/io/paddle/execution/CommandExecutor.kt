package io.paddle.execution

import java.io.File

abstract class CommandExecutor(val stdConfiguration: StdConfiguration = StdConfiguration()) {
    data class StdConfiguration(val printStdOut: Boolean = true, val printStdErr: Boolean = true)

    abstract fun execute(command: String, args: Iterable<String>, working: File): Int
}
