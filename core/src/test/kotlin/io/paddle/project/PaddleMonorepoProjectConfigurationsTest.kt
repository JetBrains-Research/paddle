package io.paddle.project

import io.paddle.project.extensions.descriptor
import io.paddle.project.extensions.routeAsString
import io.paddle.utils.deepResolve
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

internal class PaddleMonorepoProjectConfigurationsTest {
    private val resources: File = File("src").deepResolve("test", "resources")

    @Test
    fun `test deep monorepo with nested subprojects configuration`() {
        val rootDir = resources.resolve("deep-monorepo")
        val projectProvider = PaddleProjectProvider.getInstance(rootDir)

        val monorepo = projectProvider.getProject(rootDir)
        val subproject = projectProvider.getProject(rootDir.resolve("subproject"))
        val dependency = projectProvider.getProject(rootDir.deepResolve("subproject", "dependency-with-name"))

        assertNotNull(monorepo)
        assertNotNull(subproject)
        assertNotNull(dependency)

        assertEquals("monorepo", monorepo.descriptor.name)
        assertEquals("subproject", subproject.descriptor.name)
        assertEquals("dependency", dependency.descriptor.name)

        assertEquals(setOf(subproject, dependency), monorepo.subprojects.toSet())
        assertEquals(setOf(dependency), subproject.subprojects.toSet())

        assertEquals(":monorepo", monorepo.routeAsString)
        assertEquals(":monorepo:subproject", subproject.routeAsString)
        assertEquals(":monorepo:subproject:dependency", dependency.routeAsString)

        assertEquals(":monorepo:subproject:dependency:clean", dependency.tasks.get("clean")?.taskRoute)
        assertEquals(":monorepo:subproject:dependency:cleanAll", dependency.tasks.get("cleanAll")?.taskRoute)
    }

    @Test
    fun `test flat monorepo configuration`() {
        val rootDir = resources.resolve("flat-monorepo")
        val projectProvider = PaddleProjectProvider.getInstance(rootDir)

        val monorepo = projectProvider.getProject(rootDir)
        val subprojectOne = projectProvider.getProject(rootDir.resolve("subproject-one"))
        val subprojectTwo = projectProvider.getProject(rootDir.resolve("subproject-two"))
        val common = projectProvider.getProject(rootDir.deepResolve("some-dir", "common"))

        val nonexistentProject = projectProvider.getProject(rootDir.resolve("subproject-three"))
        assertNull(nonexistentProject)

        assertNotNull(monorepo)
        assertNotNull(subprojectOne)
        assertNotNull(subprojectTwo)
        assertNotNull(common)

        assertEquals("monorepo", monorepo.descriptor.name)
        assertEquals("subproject-one", subprojectOne.descriptor.name)
        assertEquals("subproject-two", subprojectTwo.descriptor.name)
        assertEquals("common", common.descriptor.name)

        assertEquals(setOf(subprojectOne, subprojectTwo), monorepo.subprojects.toSet())
        assertEquals(setOf(common), subprojectOne.subprojects.toSet())
        assertEquals(setOf(common), subprojectTwo.subprojects.toSet())
        assertEquals(emptySet(), common.subprojects.toSet())

        assertEquals(":monorepo", monorepo.routeAsString)
        assertEquals(":monorepo:subproject-one", subprojectOne.routeAsString)
        assertEquals(":monorepo:subproject-two", subprojectTwo.routeAsString)
        assertEquals(":monorepo:subproject-one:common", common.routeAsString)
    }
}