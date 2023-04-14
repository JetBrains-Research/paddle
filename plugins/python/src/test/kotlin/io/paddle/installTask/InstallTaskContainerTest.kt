package io.paddle.installTask

import io.paddle.plugin.python.dependencies.interpreter.InterpreterVersion
import io.paddle.plugin.python.extensions.globalInterpreter
import io.paddle.project.PaddleProjectProvider
import io.paddle.testExecutor.AbstractTestContainerTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Testcontainers
class InstallTaskContainerTest :
    AbstractTestContainerTest("python:3.10-buster" /* TODO: change me. A container should have c/cpp compiler & make tools to build python */) {
    private lateinit var rootDir: File

    @AfterEach
    fun cleanupResources() {
        rootDir.resolve(".venv").deleteRecursively()
        rootDir.resolve(".paddle").deleteRecursively()
    }

    @Test
    fun `minimal project resolving`() {
        rootDir = resources.resolve("minimal-project")
        val projectProvider = PaddleProjectProvider.getInstance(rootDir)
        val project = projectProvider.getProject(rootDir)
        assertNotNull(project)

        assertEquals("minimal-project", project.config.get<String>("project"))
        assertNotNull(project.tasks.get("install"))
    }

    @Test
    fun `install task local successful`() {
        rootDir = resources.resolve("minimal-project")
        println(container.getContainerId())
        val projectProvider = PaddleProjectProvider.getInstance(rootDir)
        val project = projectProvider.getProject(rootDir)
        assertNotNull(project)
        project.executor = executor

        project.execute("install")
        assert(project.globalInterpreter.cachedVersions.isEmpty())
        assert(project.globalInterpreter.pythonVersion.matches(InterpreterVersion("3.10")))
    }

    @Test
    fun `install task install successful`() {
        rootDir = resources.resolve("minimal-project-3.9")
        println(container.getContainerId())
        val projectProvider = PaddleProjectProvider.getInstance(rootDir)
        val project = projectProvider.getProject(rootDir)
        assertNotNull(project)
        project.executor = executor

        project.execute("install")
        assertEquals(1, project.globalInterpreter.cachedVersions.size)
        assert(project.globalInterpreter.cachedVersions.first().matches(InterpreterVersion("3.9")))
        assert(project.globalInterpreter.pythonVersion.matches(InterpreterVersion("3.9")))
    }
}
