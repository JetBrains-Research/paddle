package io.paddle.project

import io.paddle.plugin.standard.extensions.Subprojects
import io.paddle.plugin.standard.extensions.plugins
import io.paddle.terminal.TextOutput
import io.paddle.utils.config.Configuration
import io.paddle.utils.hash.FileHashable
import java.io.File

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

    private val cache = HashMap<String, Project>()

    /**
     * This method creates and initializes [Project] and all its subprojects.
     */
    fun initializeProject(output: TextOutput = TextOutput.Console): Project {
        val rootBuildFile = rootDir.resolve("paddle.yaml").takeIf { it.exists() }
            ?: throw Project.ProjectInitializationException("Root project does not have paddle.yaml file")
        val rootProject = getOrCreate(workDir = rootBuildFile.parentFile, output = output)
        for (project in rootProject.projectByName.values) {
            project.extensions.register(Subprojects.Extension.key, Subprojects.Extension.create(project))
        }
        return rootProject
    }

    /**
     * This method only creates [Project]. If the project was already create, it uses internal [cache].
     */
    fun getOrCreate(
        workDir: File = File("."),
        output: TextOutput = TextOutput.Console
    ): Project {
        return cache.getOrPut(workDir.canonicalPath) {
            Project(config = Configuration.from(workDir.resolve("paddle.yaml")), workDir, rootDir, output).also {
                it.register(it.plugins.enabled)
            }
        }
    }

    fun findBy(workDir: File): Project? = cache[workDir.canonicalPath]

    fun hasProjectsIn(dir: File): Boolean = cache.keys.any { it.startsWith(dir.canonicalPath) }
}
