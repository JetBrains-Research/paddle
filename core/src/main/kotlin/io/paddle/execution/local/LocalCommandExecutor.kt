package io.paddle.execution.local

import io.paddle.execution.CommandExecutor
import io.paddle.execution.ExecutionResult
import io.paddle.terminal.Terminal
import io.paddle.terminal.TextOutput
import org.codehaus.plexus.util.cli.*
import java.io.File

class LocalCommandExecutor(output: TextOutput) : CommandExecutor(OutputConfiguration(output)) {
    override fun execute(command: String, args: Iterable<String>, workingDir: File, terminal: Terminal): ExecutionResult {
        return ExecutionResult(
            CommandLineUtils.executeCommandLine(
                Commandline().apply {
                    workingDirectory = workingDir
                    executable = command
                    addArguments(args.toList().toTypedArray())
                }, getConsumer(configuration.printStdOut, terminal), getConsumer(configuration.printStdErr, terminal)
            )
        )
    }

    private fun getConsumer(redirectOutput: Boolean, terminal: Terminal): StreamConsumer {
        if (!redirectOutput) {
            return StreamConsumer { }
        }
        return StreamConsumer { terminal.stdout(it + "\n") }
    }
}
