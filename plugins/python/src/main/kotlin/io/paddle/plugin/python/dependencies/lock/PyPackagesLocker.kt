package io.paddle.plugin.python.dependencies.lock

import io.paddle.plugin.python.dependencies.index.*
import io.paddle.plugin.python.extensions.*
import io.paddle.project.Project
import kotlinx.coroutines.runBlocking

object PyPackagesLocker {
    fun lock(project: Project) = runBlocking {
        val lockFile = PyLockFile()
        for (pkg in project.requirements.resolved) {
            val metadata = PyPackagesRepositoryIndexer.downloadMetadata(pkg)
            lockFile.addLockedPackage(pkg, metadata)
        }
        lockFile.save(project.workDir.toPath())
    }

    suspend fun installFromLock(project: Project) {
        val lockFile = project.workDir.resolve(PyLockFile.FILENAME)
        if (!lockFile.exists()) {
            error("${PyLockFile.FILENAME} was not found in the project.")
        }

        val lockedPackages = PyLockFile.fromFile(lockFile).packages
        for (lockedPkg in lockedPackages) {
            val repo = PyPackagesRepository(lockedPkg.repoUrl, lockedPkg.repoName)
            val distUrl = PyDistributionsResolver.resolve(lockedPkg.name, lockedPkg.version, repo, project)
                ?.substringBefore("#") // drop anchors since hashes are compared separately later
                ?: error("Could not resolve '${lockedPkg.name}' ${lockedPkg.version} within specified repo: ${lockedPkg.repoUrl}")
            val pkg = PyPackage(
                descriptor = Requirements.Descriptor(lockedPkg.name, lockedPkg.version, lockedPkg.repoName),
                repo = repo,
                distributionUrl = distUrl
            )
            val metadata = PyPackagesRepositoryIndexer.downloadMetadata(pkg)
            val availableDistributions = metadata.releases[pkg.version]
                ?: error("Locked distribution $pkg was not found in current package metadata. Consider upgrading your lockfile.")
            val currentHash = availableDistributions.find { it.url == distUrl }?.packageHash

            if (currentHash !in lockedPkg.distributions.map { it.hash }) {
                error("Can not find appropriate distribution in the lockfile for ${distUrl}: inconsistent hashes.")
            }

            project.environment.install(pkg)
        }
    }
}
