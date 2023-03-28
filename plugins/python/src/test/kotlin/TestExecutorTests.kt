import io.paddle.terminal.Terminal
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.startupcheck.IsRunningStartupCheckStrategy
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.io.File


@Testcontainers
class TestExecutorTests {
    @Container
    var container: GenericContainer<*> = GenericContainer(DockerImageName.parse("ubuntu:latest"))
        .withCommand("tail -f /dev/null") // a stub command to keep container alive
        .withStartupCheckStrategy(IsRunningStartupCheckStrategy())

    @Test
    fun `echo executor test`() {
        val executor = TestContainerExecutor(container)
        val terminal = Terminal(TestConsole())
        assert(container.isRunning)
        executor.execute("echo", emptyList(), File("not_exists"), terminal).orElseDo { fail("The test failed with non-zero $it") }
        assert(container.isRunning())
    }

    @Test
    fun `args in testExecutor`() {
        val executor = TestContainerExecutor(container)
        val console = TestConsole()
        val terminal = Terminal(console)
        assert(container.isRunning)
        executor
            .execute("echo", listOf("-n", "a", "b", "c", "d"), File("stub"), terminal, verbose = false)
            .expose(
                onSuccess = { _ ->
                    assertEquals("a b c d", console.stdout.trim())
                    assert(console.stderr.trim().isEmpty())
                },
                onFail = { code -> fail("The echo failed with code $code") }
            )
    }

    @Test
    fun `envs in TestExecutor`() {
        val executor = TestContainerExecutor(container)
        val console = TestConsole()
        val terminal = Terminal(console)
        val args = listOf("\$ENV1", "\$ENV2", "\$UNKNOWN")
        val envs = mapOf("ENV1" to "A", "ENV2" to "B")
        assert(container.isRunning)

        executor
            .execute(
                "echo",
                args = args,
                workingDir = File("stub"),
                terminal = terminal,
                verbose = false,
                env = envs
            )
            .expose(onSuccess = { _ ->
                assertEquals("\$ENV1 \$ENV2 \$UNKNOWN", console.stdout.trim())
                assert(console.stderr.trim().isEmpty())
            },
                onFail = { code -> fail("The echo failed with code $code") })
        console.clear()
        executor
            .execute(
                "printenv",
                args = listOf("ENV1", "ENV2", "UNKNOWN"),
                workingDir = File("stub"),
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

                        else -> fail("The printenv failed with code $code. stderr: ${console.stderr}")
                    }
                })
        console.clear()
        executor
            .execute(
                "printenv",
                args = listOf("ENV1"),
                workingDir = File("stub"),
                terminal = terminal,
                verbose = false,
                env = emptyMap()
            )
            .expose(
                onSuccess = {_ ->
                    fail("Unexcepted success")
                },
                onFail = {code ->
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
}
