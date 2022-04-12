package io.paddle.project

import io.paddle.plugin.standard.extensions.*
import io.paddle.terminal.TextOutput
import io.paddle.utils.config.Configuration
import io.paddle.utils.hash.FileHashable
import io.paddle.utils.isPaddle
import java.io.File

val Project.provider: ProjectProvider
    get() = ProjectProvider.getInstance(this.rootDir)

class ProjectProvider private constructor(private val rootDir: File) {
    companion object {
        private val instances = HashMap<String, ProjectProvider>()

        /**
         * Root directory structure could change because of some refactoring, so we need to verify that it remains the same via [FileHashable].
         */
        fun getInstance(rootDir: File): ProjectProvider {
            return instances.getOrPut(FileHashable(rootDir).hash()) { ProjectProvider(rootDir) }
        }
    }

    private val projectByWorkDir = HashMap<String, Project>()
    private val projectByName = HashMap<String, Project>()

    /**
     * This method creates and initializes [Project] and all its subprojects.
     */
    fun initializeProject(output: TextOutput = TextOutput.Console): Project {
        val rootBuildFile = rootDir.resolve("paddle.yaml").takeIf { it.exists() }
            ?: throw Project.ProjectInitializationException("Root project does not have paddle.yaml file")

        val rootProject = getOrCreate(workDir = rootBuildFile.parentFile, output = output)

        projectByName.putAll(
            rootDir.walkTopDown()
                .filter { it.isPaddle }
                .map { getOrCreate(workDir = it.parentFile, output = output) }
                .associateBy { it.descriptor.name }
        )

        for (project in projectByName.values) {
            project.extensions.register(Subprojects.Extension.key, Subprojects.Extension.create(project))
        }

        return rootProject
    }

    /**
     * This method only creates [Project]. If the project was already create, it uses internal [projectByWorkDir].
     */
    private fun getOrCreate(
        workDir: File = File("."),
        output: TextOutput = TextOutput.Console
    ): Project {
        return projectByWorkDir.getOrPut(workDir.canonicalPath) {
            Project(config = Configuration.from(workDir.resolve("paddle.yaml")), workDir, rootDir, output).also {
                it.register(it.plugins.enabled)
            }
        }
    }

    val rootProject: Project?
        get() = projectByWorkDir[rootDir.canonicalPath]

    fun findBy(workDir: File): Project? = projectByWorkDir[workDir.canonicalPath]

    fun findBy(name: String): Project? = projectByName[name]

    fun hasProjectsIn(dir: File): Boolean = projectByWorkDir.keys.any { it.startsWith(dir.canonicalPath) }
}
