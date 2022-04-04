package io.paddle.plugin.python.tasks.env

import io.paddle.plugin.python.dependencies.lock.PyLockFile
import io.paddle.plugin.python.dependencies.lock.PyPackageLocker
import io.paddle.plugin.python.extensions.repositories
import io.paddle.plugin.python.extensions.requirements
import io.paddle.plugin.standard.extensions.subprojects
import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import io.paddle.utils.tasks.TaskDefaultGroups
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

class LockTask(project: Project) : IncrementalTask(project) {
    override val id: String = "lock"

    override val group: String = TaskDefaultGroups.BUILD

    override val inputs: List<Hashable> = listOf(project.repositories, project.requirements)
    override val outputs: List<Hashable> = listOf(project.workDir.resolve(PyLockFile.FILENAME).hashable())

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("install")) + project.subprojects.getAllTasksById(this.id)

    override fun act() = runBlocking {
        project.terminal.info("Locking dependencies...")
        val duration = measureTimeMillis { PyPackageLocker.lock(project) }
        project.terminal.info("Finished: ${duration}ms")
    }
}
