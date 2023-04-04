package io.paddle.testExecutor

import io.paddle.terminal.Terminal
import io.paddle.utils.deepResolve
import org.junit.jupiter.api.BeforeEach
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.startupcheck.IsRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.io.File
import kotlin.io.path.Path
@Testcontainers
open class AbstractTestContainerTest(val containerName: String) {
    protected val resources: File = File("src").deepResolve("test", "resources")
    private val mountedPath = Path("/tmp/resources")

    @Container
    protected var container: GenericContainer<*> = GenericContainer(DockerImageName.parse(containerName))
        .withCommand("tail -f /dev/null") // a stub command to keep container alive
        .withStartupCheckStrategy(IsRunningStartupCheckStrategy())
        .withFileSystemBind(resources.absolutePath, mountedPath.toString(), BindMode.READ_WRITE)

    protected lateinit var executor: TestContainerExecutor
    protected lateinit var console: TestConsole
    protected lateinit var terminal: Terminal

    @BeforeEach
    fun executorInit() {
        executor = TestContainerExecutor(container, resources, mountedPath)
        console = TestConsole()
        terminal = Terminal(console)
    }
}
