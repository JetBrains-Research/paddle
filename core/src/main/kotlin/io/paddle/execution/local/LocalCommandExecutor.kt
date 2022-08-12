package io.paddle.execution.local

import io.paddle.execution.CommandExecutor
import io.paddle.execution.ExecutionResult
import io.paddle.terminal.Terminal
import io.paddle.terminal.TextOutput
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.codehaus.plexus.util.cli.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Consumer

open class LocalCommandExecutor(output: TextOutput) : CommandExecutor(OutputConfiguration(output)) {
    override val runningProcesses: MutableSet<Process> = ConcurrentHashMap.newKeySet()

    override fun execute(
        command: String,
        args: Iterable<String>,
        workingDir: File,
        terminal: Terminal,
        envVars: Map<String, String>,
        verbose: Boolean
    ): ExecutionResult {
        return execute(
            command, args, workingDir, terminal, envVars, verbose,
            systemOut = { if (configuration.printStdout) terminal.stdout(it + "\n") },
            systemErr = { if (configuration.printStderr) terminal.stderr(it + "\n") },
        )
    }

    override fun execute(
        command: String,
        args: Iterable<String>,
        workingDir: File,
        terminal: Terminal,
        envVars: Map<String, String>,
        verbose: Boolean,
        systemOut: Consumer<String>,
        systemErr: Consumer<String>
    ): ExecutionResult = runBlocking {
        if (verbose) {
            terminal.info("${workingDir.path} % $command ${args.joinToString(" ")}")
        }

        yield()

        return@runBlocking ExecutionResult(
            executeCommandLine(
                Commandline().apply {
                    envVars.forEach { addEnvironment(it.key, it.value) }
                    workingDirectory = workingDir
                    executable = command
                    addArguments(args.toList().toTypedArray())
                },
                { systemOut.accept(it) },
                { systemErr.accept(it) }
            )
        )
    }

    private fun executeCommandLine(
        cl: Commandline,
        systemOut: StreamConsumer,
        systemErr: StreamConsumer,
    ): Int {
        val process = cl.execute()
        runningProcesses.add(process)

        val processHook: Thread = object : Thread() {
            init {
                name = "CommandLineUtils process shutdown hook"
                contextClassLoader = null
            }

            override fun run() {
                runningProcesses.remove(process)
                process.destroy()
            }
        }
        ShutdownHookUtils.addShutDownHookSilently(processHook)

        var outputPumper: StreamPumper? = null
        var errorPumper: StreamPumper? = null
        var success = false
        try {
            outputPumper = StreamPumper(process.inputStream, systemOut).apply { start() }
            errorPumper = StreamPumper(process.errorStream, systemErr).apply { start() }

            val returnValue = process.waitFor()

            outputPumper.waitUntilDone()
            errorPumper.waitUntilDone()
            outputPumper.close()
            handleException(outputPumper, "stdout")
            errorPumper.close()
            handleException(errorPumper, "stderr")

            success = true

            return returnValue
        } catch (ex: InterruptedException) {
            throw CommandLineTimeOutException("Error while executing external command, process killed.", ex)
        } finally {
            outputPumper?.disable()
            errorPumper?.disable()

            try {
                ShutdownHookUtils.removeShutdownHookSilently(processHook)
                processHook.run()
            } finally {
                try {
                    if (outputPumper != null) {
                        outputPumper.close()
                        if (success) {
                            success = false
                            handleException(outputPumper, "stdout")
                            success = true // Only reached when no exception has been thrown.
                        }
                    }
                } finally {
                    if (errorPumper != null) {
                        errorPumper.close()
                        if (success) {
                            handleException(errorPumper, "stderr")
                        }
                    }
                }
            }
        }
    }

    private fun handleException(streamPumper: StreamPumper, streamName: String) {
        if (streamPumper.exception != null) {
            throw CommandLineException(
                String.format("Failure processing %s.", streamName),
                streamPumper.exception
            )
        }
    }
}
