package io.paddle.resolveInterpreterTests

import io.paddle.plugin.python.dependencies.interpreter.InterpreterVersion
import io.paddle.plugin.python.extensions.globalInterpreter
import io.paddle.project.PaddleProject
import io.paddle.testExecutor.AbstractTestContainerTest
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@Testcontainers
class ResolveInterpreterContainerTest:
    AbstractTestContainerTest("python:3.10-buster" /* TODO: change me. A container should have c/cpp compiler & make tools to build python */) {
    private lateinit var rootDir: File

    @AfterEach
    fun cleanupResources() {
        rootDir.resolve(".venv").deleteRecursively()
        rootDir.resolve(".paddle").deleteRecursively()
    }
    @ParameterizedTest
    @ValueSource(strings = ["minimal-project", "minimal-project-3.9"])
    fun `minimal project resolving`(projectName: String) {
        rootDir = resources.resolve(projectName)
        val project = PaddleProject.create(rootDir)
        assertNotNull(project)

        assertEquals(projectName, project.config.get<String>("project"))
        assertNotNull(project.tasks.get("resolveInterpreter"))
    }

    @Test
    fun `install task local successful`() {
        rootDir = resources.resolve("minimal-project")
        val project = PaddleProject.create(rootDir)

        assertNotNull(project)
        project.executor = executor

        project.execute("resolveInterpreter")
        assert(project.globalInterpreter.cachedVersions.isEmpty())
        assert(project.globalInterpreter.pythonVersion.matches(InterpreterVersion("3.10")))
    }

    @Test
    @Order(1)
    fun `install task install successful`() {
        rootDir = resources.resolve("minimal-project-3.9")
        val project = PaddleProject.create(rootDir)

        assertNotNull(project)
        project.executor = executor
        assertEquals(0, project.globalInterpreter.cachedVersions.size)

        project.execute("resolveInterpreter")
        assertEquals(1, project.globalInterpreter.cachedVersions.size)
        assert(project.globalInterpreter.cachedVersions.first().matches(InterpreterVersion("3.9")))
        assert(project.globalInterpreter.pythonVersion.matches(InterpreterVersion("3.9")))
    }

    @Test
    @Order(2)
    fun `cached installation is working fine`() {
        rootDir = resources.resolve("minimal-project-3.9")
        val project = PaddleProject.create(rootDir)

        assertNotNull(project)
        project.executor = executor

        assertEquals(1, project.globalInterpreter.cachedVersions.size)
        assert(project.globalInterpreter.cachedVersions.first().matches(InterpreterVersion("3.9")))
        assert(project.globalInterpreter.pythonVersion.matches(InterpreterVersion("3.9")))
        project.execute("resolveInterpreter")
    }
}
