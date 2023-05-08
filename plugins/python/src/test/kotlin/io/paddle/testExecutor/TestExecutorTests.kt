package io.paddle.testExecutor

import io.paddle.utils.config.PaddleApplicationSettings
import io.paddle.utils.deepResolve
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.koin.test.get
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.io.path.*


@Testcontainers
class TestExecutorTests : AbstractTestContainerTest("ubuntu:latest") {
    private val rootDir = resources.resolve("executorTests")



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

    @Test
    fun `paddle home path is correct`() {
        assertEquals(resources.resolve(".paddle"), paddleHome)
        assertEquals(paddleHome.toPath().absolutePathString(), this.get<PaddleApplicationSettings.PaddleHomeProvider>().getPath().absolutePathString())
        assertEquals(paddleHome.toPath().absolutePathString(), PaddleApplicationSettings.paddleHome.absolutePathString())
    }

    private fun failWithCode(cmd: String, code: Int) {
        fail("$cmd failed with code $code.\nStdout: ${console.stdout}\nStderr:${console.stderr}")
    }
}
