package io.paddle.execution

import io.paddle.terminal.Terminal
import io.paddle.terminal.TextOutput
import java.io.File
import java.util.function.Consumer

abstract class CommandExecutor(val configuration: OutputConfiguration) {
    data class OutputConfiguration(
        val output: TextOutput,
        val printStdout: Boolean = true,
        val printStderr: Boolean = true
    )

    open val runningProcesses: MutableSet<Process> = hashSetOf()

    abstract fun execute(
        command: String,
        args: Iterable<String>,
        workingDir: File,
        terminal: Terminal,
        envVars: Map<String, String> = emptyMap(),
        verbose: Boolean = true
    ): ExecutionResult

    abstract fun execute(
        command: String,
        args: Iterable<String>,
        workingDir: File,
        terminal: Terminal,
        envVars: Map<String, String> = emptyMap(),
        verbose: Boolean = true,
        systemOut: Consumer<String>,
        systemErr: Consumer<String>
    ): ExecutionResult
}

class ExecutionResult(private val code: Int) {
    fun then(action: (Int) -> ExecutionResult): ExecutionResult =
        if (code == 0) action.invoke(code)
        else this

    fun orElse(action: (Int) -> ExecutionResult): ExecutionResult =
        if (code == 0) this
        else action.invoke(code)

    fun orElseDo(onFail: (Int) -> Unit) {
        if (code != 0) {
            onFail.invoke(code)
        }
    }

    fun <T> expose(onSuccess: (Int) -> T, onFail: (Int) -> T): T =
        if (code == 0) onSuccess.invoke(code)
        else onFail.invoke(code)
}
