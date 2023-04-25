package io.paddle.testExecutor

import io.paddle.execution.local.LocalCommandExecutor
import io.paddle.terminal.Terminal
import io.paddle.testExecutor.AbstractTestContainerTest.Companion.paddleHome
import io.paddle.testExecutor.AbstractTestContainerTest.Companion.resources
import io.paddle.utils.config.PaddleAppRuntime
import io.paddle.utils.config.PaddleApplicationSettings
import io.paddle.utils.deepResolve
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.OutputFrame
import org.testcontainers.containers.output.ToStringConsumer
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.nio.file.Path
import java.time.Duration

/**
 * An abstract test, that executes in a docker container
 *
 * To use just inherit your test class from this class. It mounts resources folder
 * and creates a local paddle home directory, that is not associated with your PADDLE_HOME.
 *
 * **NB**: the paddle home directory is not clearing between test, but the project folders (.venv, project's .paddle) are.
 * @param containerName Docker's container name:version, that will be pulled locally(?) or from Docker Hub
 * @property resources A test resources folder
 * @property paddleHome A local paddle home folder, that is used
 * @property container Currently running container abstraction, that set to run until test completes,
 * set user with your user permissions and mounted [resources] and [paddleHome] with your host's paths, so in code you can use your local paths
 * @property console Every test refreshing console, that holds stdout and stderr
 * @property terminal Every test refreshing terminal, that used in [LocalCommandExecutor.execute]
 * @property executor Every test refreshing executor, that run command in the docker container
 * @property logConsumer Container's log consumer
 */
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
    protected var container: GenericContainer<*> =
        GenericContainer(ImageFromDockerfile()
            .withDockerfileFromBuilder {
                val cmdExecutor = LocalCommandExecutor()
                val output = mutableListOf<String>()
                cmdExecutor.execute("id",
                    args = listOf("-u"),
                    workingDir = resources,
                    terminal = Terminal.MOCK,
                    systemOut = { output.add(it) }
                )
                val USER_ID = output[0].trim()
                output.clear()
                cmdExecutor.execute("id",
                    args = listOf("-g"),
                    workingDir = resources,
                    terminal = Terminal.MOCK,
                    systemOut = { output.add(it) }
                )
                val GROUP_ID = output[0].trim()
                it
                    .from(containerName)
                    .run("addgroup --gid $GROUP_ID user")
                    .run("adduser --disabled-password --gecos '' --uid $USER_ID --gid $GROUP_ID user")
                    .user("user")
            })
            .withCommand("tail -f /dev/null") // a stub command to keep container alive
            .withFileSystemBind(resources.absolutePath, resources.absolutePath, BindMode.READ_WRITE)
            .withFileSystemBind(paddleHome.absolutePath, paddleHome.absolutePath, BindMode.READ_WRITE)
            .withStartupTimeout(Duration.ofHours(1))

    protected lateinit var executor: TestContainerExecutor
    protected lateinit var console: TestConsole
    protected lateinit var terminal: Terminal

    protected lateinit var logConsumer: ToStringConsumer

    /**
     * Initialize test environment
     */
    @BeforeEach
    fun executorInit() {
        executor = TestContainerExecutor(container)
        console = TestConsole()
        terminal = Terminal(console)
        logConsumer = ToStringConsumer()
        container.followOutput(logConsumer, OutputFrame.OutputType.STDOUT)
        assert(container.isRunning)
        // FIXME: this make TestContainer fail without error and with empty log
//        paddleHome.listFiles()?.forEach {
//            it.deleteRecursively()
//        }
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

        @JvmStatic
        protected val paddleHome: File = resources.resolve(".paddle")

        /**
         * Cleanup .paddleHome after all tests
         */
        @JvmStatic
        @AfterAll
        fun deletePaddleHome() {
            paddleHome.deleteRecursively()
        }

        @JvmStatic
        @BeforeAll
        fun assertTestMode() {
            assert(PaddleAppRuntime.isTests)
        }
    }
}
