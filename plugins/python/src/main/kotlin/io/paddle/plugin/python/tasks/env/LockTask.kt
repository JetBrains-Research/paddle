package io.paddle.plugin.python.tasks.env

import io.paddle.plugin.python.dependencies.lock.PyLockFile
import io.paddle.plugin.python.dependencies.lock.PyPackagesLocker
import io.paddle.plugin.python.extensions.*
import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.Hashable
import io.paddle.utils.hashable
import io.paddle.utils.tasks.TaskDefaultGroups

class LockTask(project: Project) : IncrementalTask(project) {
    override val id: String = "lock"

    override val group: String = TaskDefaultGroups.BUILD

    override val inputs: List<Hashable> = listOf(project.environment, project.repositories, project.requirements)
    override val outputs: List<Hashable> = listOf(project.workDir.resolve(PyLockFile.FILENAME).hashable())

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("install"))

    override fun initialize() {
    }

    override fun act() {
        PyPackagesLocker.lock(project)
    }
}
