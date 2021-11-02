package io.paddle.plugin.python.dependencies.lock

import io.paddle.plugin.python.dependencies.GlobalVenvManager
import io.paddle.plugin.python.extensions.requirements
import io.paddle.project.Project
import kotlinx.coroutines.runBlocking

object PyPackagesLocker {

    fun lock(project: Project) = runBlocking {
        val repositories = project.requirements.repositories
        val lockFile = PyLockFile()
        for (descriptor in project.requirements.descriptors) {
            val (repo, distribution) = repositories.find(descriptor) ?: error("Can not find available repository with package: $descriptor")
            val distHash = "HASH OF THE DOWNLOADED DIST FROM GLOBAL CACHE" // TODO
            lockFile.addLockedPackage(distribution, repo, distHash)
        }
        lockFile.save(project.workDir.toPath())
    }

}
