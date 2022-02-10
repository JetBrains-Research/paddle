package io.paddle.plugin.ssh

import com.github.fracpete.processoutput4j.output.StreamingProcessOutput
import com.github.fracpete.rsync4j.RSync
import com.github.fracpete.rsync4j.Ssh
import io.paddle.execution.CommandExecutor
import io.paddle.execution.ExecutionResult
import io.paddle.plugin.ssh.output.RemoteOutputOwner
import io.paddle.project.Project
import io.paddle.terminal.Terminal
import io.paddle.terminal.TextOutput
import io.paddle.utils.ext.Extendable
import java.io.File

class SshCommandExecutor(private val host: String, private val user: String,
                         remoteDir: String, output: TextOutput) : CommandExecutor(OutputConfiguration(output)) {
    object Extension : Project.Extension<SshCommandExecutor> {
        override val key: Extendable.Key<SshCommandExecutor> = Extendable.Key()

        override fun create(project: Project): SshCommandExecutor {
            val host: String? = project.config.get("executor.host")
            val user: String? = project.config.get("executor.user")
            val dir: String? = project.config.get("executor.directory")
            return SshCommandExecutor(host!!, user!!, dir!!, project.output)
        }
    }

    private val remoteDir = if (remoteDir.endsWith("/")) remoteDir else "$remoteDir/"

    override fun execute(command: String, args: Iterable<String>, workingDir: File, terminal: Terminal): ExecutionResult {
        terminal.stdout(
            "> Executor :remote-ssh: ${
                Terminal.colored(
                    "Transfer files via rsync from ${workingDir.canonicalPath} to $remoteDir with host: $host and username: $user", Terminal.Color.CYAN
                )
            }"
        )
        val rsyncTo = RSync()
            .archive(true)
            .recursive(true)
            .ignoreExisting(true)
            .verbose(true)
            .compress(true)
            .rsh("ssh")
            .source("${workingDir.canonicalPath}/")
            .destination("${user}@${host}:$remoteDir")

        val processOutput = StreamingProcessOutput(RemoteOutputOwner(terminal))
        processOutput.monitor(rsyncTo.builder())
        rsyncTo.start().waitFor()

        val commandPath = File(command)
        val remotePath = File(remoteDir)
        val fixedCommand =
            if (commandPath.startsWith(workingDir)) remotePath.resolve(commandPath.relativeTo(workingDir)).canonicalPath
            else commandPath
        val fixedArgs = args.map {
            val argPath = File(it)
            if (argPath.startsWith(workingDir)) remotePath.resolve(argPath.relativeTo(workingDir)).canonicalPath
                else argPath
        }

        terminal.stdout("> Executor :remote-ssh: ${Terminal.colored(
            "Execute commands via ssh with host: $host and username: $user", Terminal.Color.CYAN)}")

        val ssh = Ssh()
            .outputCommandline(true)
            .verbose(1)
            .loginName(user)
            .hostname(host)
            .command(fixedArgs.joinToString(" ", "$fixedCommand "))

        processOutput.monitor(ssh.builder())
        ssh.start().waitFor()

        val rsyncFrom = RSync()
            .archive(true)
            .recursive(true)
            .ignoreExisting(true)
            .verbose(true)
            .compress(true)
            .rsh("ssh")
            .source("${user}@${host}:$remoteDir")
            .destination("${workingDir.canonicalPath}/")

        processOutput.monitor(rsyncFrom.builder())
        return ExecutionResult(rsyncFrom.start().waitFor())
    }
}
