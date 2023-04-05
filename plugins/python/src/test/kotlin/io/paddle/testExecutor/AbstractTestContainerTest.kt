package io.paddle.testExecutor

import io.paddle.terminal.Terminal
import io.paddle.utils.config.PaddleApplicationSettings
import io.paddle.utils.deepResolve
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.startupcheck.IsRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path

@Testcontainers
open class AbstractTestContainerTest(containerName: String) : KoinTest {
    private class PaddleResourcesHomeProvider : PaddleApplicationSettings.PaddleHomeProvider {
        override fun getPath(): Path {
            return paddleHome.absoluteFile.toPath()
        }
    }

    init {
        if (!paddleHome.exists()) {
            paddleHome.mkdirs()
        }
    }

    @Container
    protected var container: GenericContainer<*> = GenericContainer(DockerImageName.parse(containerName))
        .withCommand("tail -f /dev/null") // a stub command to keep container alive
        .withStartupCheckStrategy(IsRunningStartupCheckStrategy())
        .withFileSystemBind(resources.absolutePath, mountedPath.toString(), BindMode.READ_WRITE)
        .withFileSystemBind(paddleHome.absolutePath, paddleHome.absolutePath, BindMode.READ_WRITE)

    protected lateinit var executor: TestContainerExecutor
    protected lateinit var console: TestConsole
    protected lateinit var terminal: Terminal

    @BeforeEach
    fun executorInit() {
        executor = TestContainerExecutor(container, resources, mountedPath)
        console = TestConsole()
        terminal = Terminal(console)
        paddleHome.listFiles()?.forEach {
            it.deleteRecursively()
        }
    }

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(module {
            single<PaddleApplicationSettings.PaddleHomeProvider> { PaddleResourcesHomeProvider() }
        })
    }

    companion object {
        @JvmStatic
        protected val resources: File = File("src").deepResolve("test", "resources")
        private val mountedPath = Path("/tmp/resources")
        @JvmStatic
        protected val paddleHome: File = resources.resolve(".paddle")
    }
}
