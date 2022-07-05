package io.paddle.project

import io.paddle.project.extensions.descriptor
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
class PaddleProjectProvider private constructor(val rootDir: File) {
    companion object {
        private val providersCache = HashMap<File, PaddleProjectProvider>()

        fun getInstance(rootDir: File): PaddleProjectProvider {
            return providersCache.getOrPut(rootDir) { PaddleProjectProvider(rootDir) }
        }
    }

    private val index = PaddleProjectIndex(rootDir)

    fun sync() {
        index.refresh(rootDir)
        index.dumbProjects.forEach { it.load(index) }
    }

    init {
        index.dumbProjects.forEach { it.load(index) }
    }

    fun getProject(workDir: File): PaddleProject? {
        return index.getProjectByWorkdir(workDir)
    }

    fun hasProjectsIn(dir: File): Boolean {
        return index.hasProjectsIn(dir)
    }

    /**
     * Suppose you have a directory called "example" with Paddle project named "example-paddle",
     * and also some Paddle projects within its subdirectory "plugins", which is not a Paddle project itself.
     * So the relative path to the plugin "etc" would be "example/plugins/etc",
     * but the route (in terms of Paddle) is ":example-paddle:plugins:etc".
     */
    fun getRouteToDir(dir: File): List<String> {
        var currentDir = dir
        val route = ArrayList<String>()
        while (currentDir != rootDir) {
            getProject(currentDir)
                ?.let { route.add(it.descriptor.name) }
                ?: route.add(currentDir.name)
            currentDir = currentDir.parentFile
        }
        return route
    }
}
