package io.paddle.project

import io.paddle.project.extensions.Descriptor
import io.paddle.project.extensions.descriptor
import io.paddle.terminal.TextOutput
import io.paddle.utils.isPaddle
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap

internal class PaddleProjectIndex(rootDir: File, val output: TextOutput) {
    private lateinit var indexByName: ConcurrentHashMap<String, PaddleProject>
    private lateinit var indexByWorkdir: ConcurrentHashMap<File, PaddleProject>
    lateinit var dumbProjects: List<PaddleProject>
        private set

    companion object {
        const val MAX_RETRY_COUNT = 3
    }

    init {
        refresh(rootDir)
    }

    fun refresh(rootDir: File) {
        dumbProjects = collectDumbProjects(rootDir, output)
        indexByName = ConcurrentHashMap(dumbProjects.associateBy { it.descriptor.name })
        indexByWorkdir = ConcurrentHashMap(dumbProjects.associateBy { it.workDir })
    }

    private fun collectDumbProjects(rootDir: File, output: TextOutput): List<PaddleProject> = runBlocking {
        var dumbProjects: List<PaddleProject>? = null
        for (attemptNum in 1..MAX_RETRY_COUNT) {
            try {
                dumbProjects = rootDir.walkTopDown()
                    .filter { it.isPaddle }
                    .map { buildFile ->
                        async {
                            PaddleProject(buildFile, rootDir, output).apply {
                                extensions.register(Descriptor.Extension.key, Descriptor.Extension.create(this))
                            }
                        }
                    }
                    .toList()
                    .awaitAll()
            } catch (e: Throwable) {
                delay(100L * (1 shl attemptNum))
                continue
            }
            break
        }
        return@runBlocking dumbProjects ?: emptyList()
    }

    fun getProjectByName(name: String): PaddleProject? {
        return indexByName[name]
    }

    fun getProjectByWorkdir(workDir: File): PaddleProject? {
        return indexByWorkdir[workDir]
    }

    fun hasProjectsIn(dir: File): Boolean {
        return indexByWorkdir.keys.any { it.canonicalPath.startsWith(dir.canonicalPath) }
    }

    class PaddleProjectIndexInitializationException(reason: String) : Exception(reason)
}
