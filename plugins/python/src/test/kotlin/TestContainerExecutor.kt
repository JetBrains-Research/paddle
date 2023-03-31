import io.kotest.common.runBlocking
import io.ktor.utils.io.core.*
import io.paddle.execution.*
import io.paddle.terminal.Terminal
import kotlinx.coroutines.yield
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.*
import org.testcontainers.containers.wait.strategy.WaitAllStrategy
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.function.Consumer

class TestContainerExecutor(private val container: GenericContainer<*>, rootDir: File, private val mountedPath: Path) : CommandExecutor {
    override val os: CommandExecutor.OsInfo
        get() = TODO("Not yet implemented")
    override val env: EnvProvider
        get() = TODO("Not yet implemented")
    override val runningProcesses: MutableSet<Process>
        get() = TODO("Not yet implemented")

    private val rootPath = rootDir.toPath()

    override fun execute(
        command: String,
        args: Iterable<String>,
        workingDir: File,
        terminal: Terminal,
        env: Map<String, String>,
        verbose: Boolean,
        systemOut: Consumer<String>,
        systemErr: Consumer<String>
    ): ExecutionResult = runBlocking {
        container.waitingFor(WaitAllStrategy())
        if (verbose) {
            terminal.info("${workingDir.path} % $command ${args.joinToString(" ")}")
        }

        yield()

        val workingPath = workingDir.toPath()
        if (!workingPath.startsWith(rootPath)) {
            error("Invalid path. Working directory `$workingPath` should be a child of $rootPath")
        }
        var suffix = rootPath.relativize(workingPath).toString()
        if (suffix.isBlank()) {
            suffix = "."
        }

        // This is rewritten version if `container.execInContainer`
        // This version support env and changing working directory

        val dockerClient = container.dockerClient
        val cmd = dockerClient.execCreateCmd(container.containerId)
            .withAttachStdout(true)
            .withAttachStderr(true)
            .withWorkingDir(mountedPath.resolve(suffix).toString())
            .withCmd(*((listOf(command) + args.toList()).toTypedArray()))
            .withEnv(env.map { (k, v) -> "$k=$v" })
            .exec()
        val stdoutConsumer = ToStringConsumer()
        val stderrConsumer = ToStringConsumer()
        FrameConsumerResultCallback().use {
            it.addConsumer(OutputFrame.OutputType.STDOUT, stdoutConsumer)
            it.addConsumer(OutputFrame.OutputType.STDERR, stderrConsumer)
            dockerClient.execStartCmd(cmd.id).exec(it).awaitCompletion()
        }

        systemOut.accept(stdoutConsumer.toString(StandardCharsets.UTF_8))
        systemErr.accept(stderrConsumer.toString(StandardCharsets.UTF_8))
        return@runBlocking ExecutionResult(dockerClient.inspectExecCmd(cmd.id).exec().exitCodeLong.toInt())
    }
}
