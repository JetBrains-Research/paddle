package io.paddle.plugin.python.dependencies.lock

import io.paddle.plugin.python.dependencies.index.PyPackagesRepositoryIndexer
import io.paddle.plugin.python.extensions.requirements
import io.paddle.project.Project
import kotlinx.coroutines.runBlocking

object PyPackagesLocker {

    fun lock(project: Project) = runBlocking {
        val lockFile = PyLockFile()
        for (descriptor in project.requirements.descriptors) {
            val metadata = PyPackagesRepositoryIndexer.downloadMetadata(descriptor.name, descriptor.repo)
            lockFile.addLockedPackage(descriptor, metadata)
        }
        lockFile.save(project.workDir.toPath())
    }

}
