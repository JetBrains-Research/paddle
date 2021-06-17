package io.paddle.terminal

import org.codehaus.plexus.util.Os
import org.codehaus.plexus.util.cli.CommandLineUtils
import org.codehaus.plexus.util.cli.Commandline
import org.codehaus.plexus.util.cli.DefaultConsumer
import org.codehaus.plexus.util.cli.StreamConsumer
import java.io.File

internal object Terminal {
    val os by lazy {
        when {
            Os.isFamily(Os.FAMILY_WINDOWS) -> "windows_amd64"
            Os.isFamily(Os.FAMILY_MAC) -> "darwin_amd64"
            Os.isFamily(Os.FAMILY_UNIX) -> "linux_amd64"
            else -> error("Unknown operating system. Probably your system is not supported.")
        }
    }

    fun execute(
        exec: String,
        args: List<String>,
        workingDir: File,
        redirectStdout: Boolean = true,
        redirectErr: Boolean = true
    ): Int {
        return CommandLineUtils.executeCommandLine(
            Commandline().apply {
                workingDirectory = workingDir
                executable = exec
                addArguments(args.toTypedArray())
            }, getConsumer(redirectStdout), getConsumer(redirectErr)
        )
    }

    fun executeOrFail(
        exec: String,
        args: List<String>,
        workingDir: File,
        redirectStdout: Boolean = false,
        redirectErr: Boolean = true
    ) {
        val returnCode = execute(exec, args, workingDir, redirectStdout, redirectErr)
        if (returnCode != 0) {
            error("Command failed: '$exec ${args.joinToString { " " }}'")
        }
    }

    private fun getConsumer(redirectOutput: Boolean): StreamConsumer {
        return if (redirectOutput)
            DefaultConsumer()
        else
            CommandLineUtils.StringStreamConsumer()
    }
}