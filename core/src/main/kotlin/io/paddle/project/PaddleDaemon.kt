package io.paddle.project

import io.paddle.plugin.standard.extensions.*
import io.paddle.utils.config.Configuration
import io.paddle.utils.isPaddle
import io.paddle.utils.lightHash
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.schedule

class PaddleDaemon private constructor(private val rootDir: File) {
    companion object {
        const val PROJECT_MODEL_SYNC_PERIOD_MS = 20_000L
        const val RETRY_COUNT = 3

        private val daemonByHash = HashMap<String, PaddleDaemon>()
        private val daemonByCanonicalPath = HashMap<String, PaddleDaemon>()

        fun getInstance(rootDir: File): PaddleDaemon {
            val hash = rootDir.lightHash()
            daemonByHash[hash]?.let { return it }

            val daemon = PaddleDaemon(rootDir).also { daemonByHash[hash] = it }
            daemonByCanonicalPath.computeIfPresent(rootDir.canonicalPath) { _, oldDaemon ->
                daemon.also { oldDaemon.stop() }
            }

            return daemon
        }
    }

    private val task: TimerTask

    init {
        task = Timer("PaddleProjectModelSynchronizer", true)
            .schedule(delay = 0, period = PROJECT_MODEL_SYNC_PERIOD_MS) { runBlocking { sync() } }
    }

    private val projectsCache = ConcurrentHashMap<String, PaddleProject>()
    private val projectByWorkDir = PaddleProjectIndex<String>()
    private val projectByName = PaddleProjectIndex<String>()
    private val initSubprojectsMutex = Mutex(locked = true)

    suspend fun sync() = coroutineScope {
        // Collect and initialize projects
        val projects = collectProjects(RETRY_COUNT) ?: return@coroutineScope

        // Update indexes
        projectByName.updateAll(projects.associateBy { it.descriptor.name })
        updateWorkDirIndex(projects)

        // Finish initialization by resolving subprojects
        initializeSubprojects(projects)
        initSubprojectsMutex.takeIf { it.isLocked }?.unlock()
    }

    fun stop() {
        task.cancel()
    }

    private suspend fun collectProjects(retryCount: Int): List<PaddleProject>? = coroutineScope {
        var projects: List<PaddleProject>? = null
        for (attemptNum in 1..retryCount) {
            try {
                projects = rootDir.walkTopDown()
                    .filter { it.isPaddle }
                    .map { buildFile ->
                        async { getOrCreateProject(buildFile, workDir = buildFile.parentFile) }
                    }
                    .toList()
                    .awaitAll()
            } catch (e: Throwable) {
                delay(100L * (1 shl attemptNum))
                continue
            }
            break
        }
        return@coroutineScope projects
    }

    private suspend fun updateWorkDirIndex(projects: List<PaddleProject>) = coroutineScope {
        val newProjectByWorkDir = ConcurrentHashMap<String, PaddleProject>()
        projects.map { project ->
            launch {
                project.workDir.lightHash().also {
                    newProjectByWorkDir[it] = project
                }
            }
        }.joinAll()
        projectByWorkDir.updateAll(newProjectByWorkDir)
    }

    private fun initializeSubprojects(projects: List<PaddleProject>) {
        for (project in projects) {
            project.extensions.register(Subprojects.Extension.key, Subprojects.Extension.create(project))
        }
    }

    private suspend fun getOrCreateProject(buildFile: File, workDir: File): PaddleProject = coroutineScope {
        return@coroutineScope projectsCache.getOrPut(workDir.lightHash()) {
            PaddleProject(config = Configuration.from(buildFile), workDir, rootDir)
                .also { it.register(it.plugins.enabled) }
        }
    }

    fun getProjectByName(name: String): PaddleProject? = runBlocking {
        return@runBlocking projectByName.getProject(name)
    }

    fun getProjectByWorkDir(workDir: File): PaddleProject? = runBlocking {
        initSubprojectsMutex.withLock {
            return@runBlocking projectByWorkDir.getProject(workDir.lightHash())
        }
    }

    fun hasProjectsIn(dir: File): Boolean = runBlocking {
        return@runBlocking projectByWorkDir.any { it.startsWith(dir.canonicalPath) }
    }
}

class PaddleProjectIndex<K> {
    private lateinit var index: Map<K, PaddleProject>
    private val mutex = Mutex(locked = true)

    suspend fun getProject(key: K): PaddleProject? {
        return mutex.withLock { index[key] }
    }

    suspend fun any(predicate: (K) -> Boolean): Boolean {
        return mutex.withLock { index.filterKeys(predicate).isNotEmpty() }
    }

    fun updateAll(newIndex: Map<K, PaddleProject>) {
        index = newIndex
        mutex.takeIf { it.isLocked }?.unlock()
    }
}
