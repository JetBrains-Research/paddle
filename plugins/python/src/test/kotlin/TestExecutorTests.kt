import io.paddle.terminal.Terminal
import io.paddle.utils.deepResolve
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.startupcheck.IsRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.createSymbolicLinkPointingTo


@Testcontainers
class TestExecutorTests {
    private val resources: File = File("src").deepResolve("test", "resources")
    private val rootDir = resources.resolve("executorTests")
    private val mountedPath = Path("/tmp/resources")

    @Container
    private var container: GenericContainer<*> = GenericContainer(DockerImageName.parse("ubuntu:latest"))
        .withCommand("tail -f /dev/null") // a stub command to keep container alive
        .withStartupCheckStrategy(IsRunningStartupCheckStrategy())
        .withFileSystemBind(resources.absolutePath, mountedPath.toString(), BindMode.READ_WRITE)

    private lateinit var executor: TestContainerExecutor
    private lateinit var console: TestConsole
    private lateinit var terminal: Terminal

    @BeforeEach
    fun executorInit() {
        executor = TestContainerExecutor(container, resources, mountedPath)
        console = TestConsole()
        terminal = Terminal(console)
    }



    @Test
    fun `echo executor test`() {
        assert(container.isRunning)
        executor.execute("echo", emptyList(), rootDir, terminal).orElseDo {
            failWithCode("echo", it)
        }
        assert(container.isRunning)
    }

    @Test
    fun `args in testExecutor`() {
        assert(container.isRunning)
        executor
            .execute("echo", listOf("-n", "a", "b", "c", "d"), rootDir, terminal, verbose = false)
            .expose(
                onSuccess = { _ ->
                    assertEquals("a b c d", console.stdout.trim())
                    assert(console.stderr.trim().isEmpty())
                },
                onFail = { code -> failWithCode("echo", code) }
            )
    }

    @Test
    fun `envs in TestExecutor`() {
        val args = listOf("\$ENV1", "\$ENV2", "\$UNKNOWN")
        val envs = mapOf("ENV1" to "A", "ENV2" to "B")
        assert(container.isRunning)

        executor
            .execute(
                "echo",
                args = args,
                workingDir = rootDir,
                terminal = terminal,
                verbose = false,
                env = envs
            )
            .expose(onSuccess = { _ ->
                assertEquals("\$ENV1 \$ENV2 \$UNKNOWN", console.stdout.trim())
                assert(console.stderr.trim().isEmpty())
            },
                onFail = { code -> failWithCode("echo", code) })
        console.clear()
        executor
            .execute(
                "printenv",
                args = listOf("ENV1", "ENV2", "UNKNOWN"),
                workingDir = rootDir,
                terminal = terminal,
                verbose = false,
                env = envs
            )
            .expose(onSuccess = { _ ->
                fail("Unexpected success")
            },
                onFail = { code ->
                    when (code) {
                        1 -> {
                            assertEquals("A\nB", console.stdout.trim())
                            assert(console.stderr.trim().isEmpty())
                        }

                        else -> failWithCode("printEnv", code)
                    }
                })
        console.clear()
        executor
            .execute(
                "printenv",
                args = listOf("ENV1"),
                workingDir = rootDir,
                terminal = terminal,
                verbose = false,
                env = emptyMap()
            )
            .expose(
                onSuccess = { _ ->
                    fail("Unexcepted success")
                },
                onFail = { code ->
                    when (code) {
                        1 -> {
                            assert(console.stdout.isBlank())
                            assert(console.stderr.isBlank())
                        }

                        else -> fail("Unexpected error code $code")
                    }
                }
            )
    }

    @Test
    fun `mounting test`() {
        executor.execute(
            "pwd",
            args = emptyList(),
            terminal = terminal,
            workingDir = rootDir,
            verbose = false
        ).expose(
            onFail = { code -> failWithCode("pwd", code) },
            onSuccess = { _ ->
                val path = Path(console.stdout.trim())
                assertEquals("executorTests", path.fileName.toString())
            })
        console.clear()
        executor.execute(
            "cat",
            args = listOf("testFile.txt"),
            terminal = terminal,
            workingDir = rootDir,
            verbose = false
        ).expose(
            onFail = { code -> failWithCode("cat", code) },
            onSuccess = { _ ->
                val content = console.stdout.trim()
                val realContent = rootDir.resolve("testFile.txt").readText().trim()
                assertEquals(realContent, content)
            }
        )
    }

    @Test
    fun `symbolic link are working correctly`() {
        val linkDir = rootDir.resolve("linkDir")
        assert(linkDir.mkdirs()) { "Could create directry linkDir" }
        val testFile = rootDir.resolve("testFile.txt")
        val link = rootDir.deepResolve("linkDir", "link").toPath()
        link.createSymbolicLinkPointingTo(linkDir.toPath().relativize(testFile.toPath()))
        try {
            console.clear()
            executor.execute(
                "cat",
                args = listOf("link"),
                terminal = terminal,
                workingDir = rootDir.resolve("linkDir"),
                verbose = false
            ).expose(
                onFail = { code -> failWithCode("cat", code) },
                onSuccess = { _ ->
                    val content = testFile.readText().trim()
                    assertEquals(content, console.stdout.trim())
                }
            )
        } finally {
            linkDir.deleteRecursively()
        }
    }

    private fun failWithCode(cmd: String, code: Int) {
        fail("$cmd failed with code $code.\nStdout: ${console.stdout}\nStderr:${console.stderr}")
    }
}
