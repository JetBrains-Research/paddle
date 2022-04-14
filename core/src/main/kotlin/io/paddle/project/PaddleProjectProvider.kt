package io.paddle.project

import io.paddle.plugin.standard.extensions.*
import io.paddle.terminal.TextOutput
import io.paddle.utils.config.Configuration
import io.paddle.utils.hash.FileHashable
import io.paddle.utils.hash.lightHashable
import io.paddle.utils.isPaddle
import java.io.File

val PaddleProject.provider: PaddleProjectProvider
    get() = PaddleProjectProvider.getInstance(this.rootDir)

class PaddleProjectProvider private constructor(private val rootDir: File) {
    companion object {
        private val instances = HashMap<String, PaddleProjectProvider>()

        /**
         * Root directory structure could change because of some refactoring, so we need to verify that it remains the same via [FileHashable].
         */
        fun getInstance(rootDir: File): PaddleProjectProvider {
            return instances.getOrPut(rootDir.lightHashable().hash()) { PaddleProjectProvider(rootDir) }
        }
    }

    private val projectByWorkDir = HashMap<String, PaddleProject>()
    private val projectByName = HashMap<String, PaddleProject>()

    init {
        projectByName.putAll(
            rootDir.walkTopDown()
                .filter { it.isPaddle }
                .map { getOrCreate(workDir = it.parentFile) }
                .associateBy { it.descriptor.name }
        )
    }

    /**
     * This method creates and initializes [PaddleProject] and all its subprojects.
     */
    fun initializeProject(output: TextOutput = TextOutput.Console): PaddleProject {
        val rootBuildFile = rootDir.resolve("paddle.yaml").takeIf { it.exists() }
            ?: throw PaddleProject.ProjectInitializationException("Root project does not have paddle.yaml file")

        val rootProject = getOrCreate(workDir = rootBuildFile.parentFile, output = output)



        for (project in projectByName.values) {
            project.extensions.register(Subprojects.Extension.key, Subprojects.Extension.create(project))
        }

        return rootProject
    }

    /**
     * This method only creates [PaddleProject]. If the project was already create, it uses internal [projectByWorkDir].
     */
    private fun getOrCreate(
        workDir: File,
        output: TextOutput = TextOutput.Console
    ): PaddleProject {
        return projectByWorkDir.getOrPut(workDir.canonicalPath) {
            PaddleProject(config = Configuration.from(workDir.resolve("paddle.yaml")), workDir, rootDir, output).also {
                it.register(it.plugins.enabled)
            }
        }
    }

    val rootProject: PaddleProject?
        get() = projectByWorkDir[rootDir.canonicalPath]

    fun findBy(workDir: File): PaddleProject? = projectByWorkDir[workDir.canonicalPath]

    fun findBy(name: String): PaddleProject? = projectByName[name]

    fun hasProjectsIn(dir: File): Boolean = projectByWorkDir.keys.any { it.startsWith(dir.canonicalPath) }
}
