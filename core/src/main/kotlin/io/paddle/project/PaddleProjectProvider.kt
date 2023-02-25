package io.paddle.project

import java.io.File

/**
 * A factory that builds and provides [PaddleProject].
 *
 * The whole approach is the following:
 *  1. Load instance of [PaddleProjectProvider] for particular [rootDir]
 *  2. Build [PaddleProjectIndex], load a collection of partially-initialized [PaddleProject]
 *  3. Build subprojects model (directed oriented graph) using index
 *  4. Adjust configuration for each project in the [rootDir]
 *      - There is a possibility to declare configurations for ALL subprojects at a time using `all` descriptor
 *  5. Register plugins (extensions & tasks) for each project in the [rootDir]
 */
class PaddleProjectProvider private constructor(val rootDir: File, val cliOptions: Map<String, String>) {
    companion object {
        private val providersCache = HashMap<File, PaddleProjectProvider>()

        fun getInstance(rootDir: File, cliOptions: Map<String, String>): PaddleProjectProvider {
            return providersCache.getOrPut(rootDir) { PaddleProjectProvider(rootDir, cliOptions) }
        }
    }

    private val index = PaddleProjectIndex(rootDir, cliOptions)

    val allProjects: Collection<PaddleProject>
        get() = index.dumbProjects.toList()

    fun sync() {
        index.refresh(rootDir, cliOptions)
        index.dumbProjects.forEach { it.load(index, cliOptions) }
    }

    init {
        index.dumbProjects.forEach { it.load(index, cliOptions) }
    }

    fun getProject(workDir: File): PaddleProject? {
        return index.getProjectByWorkdir(workDir)
    }

    fun hasProjectsIn(dir: File): Boolean {
        return index.hasProjectsIn(dir)
    }
}
