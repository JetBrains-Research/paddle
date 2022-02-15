package io.paddle.plugin.python.dependencies.lock

import io.paddle.plugin.python.dependencies.index.PyPackagesRepository
import io.paddle.plugin.python.dependencies.index.PyPackagesRepositoryIndexer
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.extensions.environment
import io.paddle.plugin.python.extensions.requirements
import io.paddle.project.Project

object PyPackagesLocker {
    suspend fun lock(project: Project) {
        val lockFile = PyLockFile()
        for (pkg in project.requirements.resolved) {
            lockFile.addLockedPackage(pkg)
        }
        lockFile.save(project.workDir.toPath())
    }

    suspend fun installFromLock(project: Project) {
        val lockFile = project.workDir.resolve(PyLockFile.FILENAME)
        if (!lockFile.exists()) {
            error("${PyLockFile.FILENAME} was not found in the project.")
        }

        val lockedPackages = PyLockFile.fromFile(lockFile).lockedPackages
        val packageByIdentifier = HashMap<LockedPyPackageIdentifier, PyPackage>()

        for (lockedPkg in lockedPackages) {
            val repo = PyPackagesRepository(lockedPkg.repoMetadata)
            val distUrl = lockedPkg.resolveConcreteDistribution(repo, project)
            val pkg = PyPackage(lockedPkg.name, lockedPkg.version, repo, distUrl)
            checkHashes(pkg, lockedPkg)
            packageByIdentifier[lockedPkg.identifier] = pkg
        }

        // Restoring inter-package dependencies via 'comesFrom' field
        for (lockedPkg in lockedPackages) {
            val comesFrom = lockedPkg.comesFrom?.let { packageByIdentifier[it] }
            packageByIdentifier[lockedPkg.identifier]!!.comesFrom = comesFrom
        }

        for (pkg in packageByIdentifier.values) {
            project.environment.install(pkg)
        }
    }

    private suspend fun checkHashes(pkg: PyPackage, lockedPkg: LockedPyPackage) {
        val metadata = PyPackagesRepositoryIndexer.downloadMetadata(pkg)
        val availableDistributions = metadata.releases[pkg.version]
            ?: error("Locked distribution $pkg was not found in current package metadata. Consider upgrading your lockfile.")
        val currentHash = availableDistributions.find { it.url == pkg.distributionUrl }?.packageHash

        if (currentHash !in lockedPkg.distributions.map { it.hash }) {
            error("Can not find appropriate distribution in the lockfile for ${pkg.distributionUrl}: inconsistent hashes.")
        }
    }
}
