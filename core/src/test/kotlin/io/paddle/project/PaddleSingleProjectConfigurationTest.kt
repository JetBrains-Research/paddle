package io.paddle.project

import io.paddle.plugin.standard.StandardPlugin
import io.paddle.plugin.standard.extensions.plugins
import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.extensions.descriptor
import io.paddle.utils.deepResolve
import java.io.File
import kotlin.test.*

internal class PaddleSingleProjectConfigurationTest {
    private val resources: File = File("src").deepResolve("test", "resources")
    private val rootDir: File = resources.resolve("single-project")
    private lateinit var project: PaddleProject

    @BeforeTest
    fun initializeProject() {
        project = checkNotNull(
            PaddleProjectProvider.getInstance(rootDir).getProject(rootDir)
        ) { "Project was not initialized" }
    }

    @Test
    fun `test single project initialization`() {
        assertEquals("single-project", project.descriptor.name)
        assertEquals(rootDir, project.rootDir)
        assertEquals(rootDir, project.workDir)
        assertEquals(rootDir.resolve("paddle.yaml"), project.buildFile)
    }

    @Test
    fun `test sources root initialization`() {
        assertEquals(rootDir.resolve("src").resolve("main"), project.roots.sources)
        assertTrue(project.roots.sources.exists())
    }

    @Test
    fun `test tests root initialization`() {
        assertEquals(rootDir.resolve("src").resolve("test"), project.roots.tests)
        assertTrue(project.roots.tests.exists())
    }

    @Test
    fun `test default resources root initialization`() {
        assertEquals(rootDir.resolve("resources"), project.roots.resources)
        assertFalse(project.roots.resources.exists())
    }

    @Test
    fun `test default distributions root initialization`() {
        assertEquals(rootDir.resolve("dist"), project.roots.dist)
        assertFalse(project.roots.resources.exists())
    }

    @Test
    fun `test standard plugin is loaded`() {
        assertEquals(listOf(StandardPlugin), project.plugins.enabled)
    }
}