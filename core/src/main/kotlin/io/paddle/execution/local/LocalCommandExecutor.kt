package io.paddle.execution.local

import io.paddle.execution.CommandExecutor
import io.paddle.execution.ExecutionResult
import io.paddle.terminal.Terminal
import io.paddle.terminal.TextOutput
import org.codehaus.plexus.util.cli.*
import java.io.File

open class LocalCommandExecutor(output: TextOutput) : CommandExecutor(OutputConfiguration(output)) {
    override fun execute(
        command: String,
        args: Iterable<String>,
        workingDir: File,
        terminal: Terminal,
        envVars: Map<String, String>,
        verbose: Boolean
    ): ExecutionResult {
        if (verbose) {
            terminal.info("${workingDir.path}$ $command ${args.joinToString(" ")}")
        }
        return ExecutionResult(
            CommandLineUtils.executeCommandLine(
                Commandline().apply {
                    envVars.forEach { addEnvironment(it.key, it.value) }
                    workingDirectory = workingDir
                    executable = command
                    addArguments(args.toList().toTypedArray())
                },
                getConsumer(configuration.printStdOut, terminal),
                getConsumer(configuration.printStdErr, terminal)
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
