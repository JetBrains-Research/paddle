package io.paddle.plugin.python.tasks.install

import io.paddle.plugin.python.dependencies.lock.PyLockFile
import io.paddle.plugin.python.dependencies.lock.PyPackageLocker
import io.paddle.plugin.python.extensions.repositories
import io.paddle.plugin.python.extensions.requirements
import io.paddle.plugin.python.tasks.PythonPluginTaskGroups
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

class LockTask(project: PaddleProject) : IncrementalTask(project) {
    override val id: String = "lock"

    override val group: String = PythonPluginTaskGroups.INSTALL

    override val inputs: List<Hashable>
        get() = listOf(project.repositories, project.requirements)
    override val outputs: List<Hashable>
        get() = listOf(project.workDir.resolve(PyLockFile.FILENAME).hashable())

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("install")) + project.subprojects.getAllTasksById(this.id)

    override fun act() = runBlocking {
        project.terminal.info("Locking dependencies...")
        val duration = measureTimeMillis { PyPackageLocker.lock(project) }
        project.terminal.info("Finished: ${duration}ms")
    }
}
