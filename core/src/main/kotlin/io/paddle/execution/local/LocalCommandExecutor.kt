package io.paddle.execution.local

import io.paddle.execution.CommandExecutor
import org.codehaus.plexus.util.cli.*
import java.io.File

class LocalCommandExecutor(): CommandExecutor() {
    override fun execute(command: String, args: Iterable<String>, working: File): Int {
        return CommandLineUtils.executeCommandLine(
            Commandline().apply {
                workingDirectory = working
                executable = command
                addArguments(args.toList().toTypedArray())
            }, getConsumer(stdConfiguration.printStdOut), getConsumer(stdConfiguration.printStdErr)
        )
    }

    private fun getConsumer(redirectOutput: Boolean): StreamConsumer {
        return if (redirectOutput)
            DefaultConsumer()
        else
            CommandLineUtils.StringStreamConsumer()
    }
}
