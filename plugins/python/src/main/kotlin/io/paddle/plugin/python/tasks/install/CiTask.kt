package io.paddle.plugin.python.tasks.install

import io.paddle.plugin.python.dependencies.GlobalCacheRepository
import io.paddle.plugin.python.dependencies.lock.PyLockFile
import io.paddle.plugin.python.dependencies.lock.PyPackageLocker
import io.paddle.plugin.python.extensions.environment
import io.paddle.plugin.python.tasks.PythonPluginTaskGroups
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

class CiTask(project: PaddleProject) : IncrementalTask(project) {
    override val id: String = "ci"

    override val group: String = PythonPluginTaskGroups.INSTALL

    override val inputs: List<Hashable>
        get() {
            val lockFile = project.workDir.resolve(PyLockFile.FILENAME)
            return if (lockFile.exists()) {
                listOf(lockFile.hashable())
            } else {
                emptyList()
            }
        }
    override val outputs: List<Hashable>
        get() = listOf(project.environment.venv.hashable())

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("venv")) + project.subprojects.getAllTasksById(this.id)

    override fun act() = runBlocking {
        project.terminal.info("Installing dependencies from ${PyLockFile.FILENAME}...")
        val duration = measureTimeMillis {
            GlobalCacheRepository.updateCache()
            PyPackageLocker.installFromLock(project)
        }
        project.terminal.info("Finished: ${duration}ms")
    }
}
