package io.paddle.plugin.python.tasks.env

import io.paddle.plugin.python.dependencies.GlobalCacheRepository
import io.paddle.plugin.python.dependencies.lock.PyLockFile
import io.paddle.plugin.python.dependencies.lock.PyPackageLocker
import io.paddle.plugin.python.extensions.environment
import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.Hashable
import io.paddle.utils.hashable
import io.paddle.utils.tasks.TaskDefaultGroups
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

class CiTask(project: Project) : IncrementalTask(project) {
    override val id: String = "ci"

    override val group: String = TaskDefaultGroups.BUILD

    override val inputs: List<Hashable>
        get() {
            val lockFile = project.workDir.resolve(PyLockFile.FILENAME)
            return if (lockFile.exists()) {
                listOf(lockFile.hashable())
            } else {
                emptyList()
            }
        }
    override val outputs: List<Hashable> = listOf(project.environment.venv.hashable())

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("venv"))

    override fun act() = runBlocking {
        project.terminal.info("Installing dependencies from ${PyLockFile.FILENAME}...")
        val duration = measureTimeMillis {
            GlobalCacheRepository.updateCache()
            PyPackageLocker.installFromLock(project)
        }
        project.terminal.info("Finished: ${duration}ms")
    }
}
