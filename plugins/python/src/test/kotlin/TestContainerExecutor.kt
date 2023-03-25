import io.kotest.common.runBlocking
import io.paddle.execution.*
import io.paddle.terminal.Terminal
import kotlinx.coroutines.yield
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.util.function.Consumer

class TestContainerExecutor : CommandExecutor {
    override val os: CommandExecutor.OsInfo
        get() = TODO("Not yet implemented")
    override val env: EnvProvider
        get() = TODO("Not yet implemented")
    override val runningProcesses: MutableSet<Process>
        get() = TODO("Not yet implemented")

    private val container = GenericContainer(DockerImageName.parse("paddle:3.10"))

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
        if (verbose) {
            terminal.info("${workingDir.path} % $command ${args.joinToString(" ")}")
        }
        yield()
        // Consider using env for running command, because test container are not support running with env
        val envString = listOf("env") + env.map { (k, v) -> "$k=$v" }
        val wholeRunCommand = envString + listOf(command) + args.toList()

        val runResult = container.execInContainer(*wholeRunCommand.toTypedArray())

        systemOut.accept(runResult.stdout)
        systemErr.accept(runResult.stderr)
        return@runBlocking ExecutionResult(runResult.exitCode)
    }
}
