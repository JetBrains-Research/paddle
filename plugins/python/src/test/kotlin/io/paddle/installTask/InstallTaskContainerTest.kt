package io.paddle.installTask

import io.paddle.project.PaddleProjectProvider
import io.paddle.testExecutor.AbstractTestContainerTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
    fun `install task successful`() {
        rootDir = resources.resolve("minimal-project")
        val projectProvider = PaddleProjectProvider.getInstance(rootDir)
        val project = projectProvider.getProject(rootDir)
        assertNotNull(project)
        project.executor = executor

        project.execute("install") // TODO: Python version is located in host system, not inside a container
    }
}
