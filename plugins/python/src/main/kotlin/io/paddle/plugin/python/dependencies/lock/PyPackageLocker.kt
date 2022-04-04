package io.paddle.plugin.python.dependencies.lock

import io.paddle.plugin.python.dependencies.PyInterpreter
import io.paddle.plugin.python.dependencies.index.PyPackageRepositoryIndexer
import io.paddle.plugin.python.dependencies.lock.models.*
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.utils.*
import io.paddle.plugin.standard.extensions.subprojects
import io.paddle.project.Project
import io.paddle.tasks.Task
import kotlinx.coroutines.supervisorScope
import java.util.concurrent.ConcurrentHashMap

object PyPackageLocker {

    suspend fun lock(project: Project) {
        supervisorScope {
            val lockedPackages = project.requirements.resolved.parallelMap { pkg ->
                val metadata = PyPackageRepositoryIndexer.downloadMetadata(pkg, project.terminal)
                val distributions = metadata?.releases?.get(pkg.version) ?: emptyList()
                LockedPyPackage(
                    LockedPyPackageIdentifier(pkg),
                    comesFrom = pkg.comesFrom?.let { LockedPyPackageIdentifier(it) },
                    distributions = distributions.map { LockedPyDistribution(it.filename, it.packageHash) }
                )
            }
            val lockFile = PyLockFile(
                interpreterVersion = project.interpreter.resolved.version.number,
                lockedPackages = lockedPackages.toSet()
            )
            lockFile.save(project.workDir.toPath())
        }
    }

    suspend fun installFromLock(project: Project) {
        val pyLockFile = PyLockFile.fromFile(project.workDir.resolve(PyLockFile.FILENAME))

        val lockedInterpreter = PyInterpreter.find(PyInterpreter.Version(pyLockFile.interpreterVersion), project)
        if (lockedInterpreter.version != project.interpreter.resolved.version) {
            error(
                "Locked interpreter version (${lockedInterpreter.version.number}) is not consistent with " +
                    "current interpreter version ${project.interpreter.resolved.version}."
            )
        }

        val packages = extractPyPackages(pyLockFile, project)
        for (pkg in packages) {
            project.environment.install(pkg)
        }

        for (subproject in project.subprojects) {
            for (pkg in subproject.requirements.resolved) {
                project.environment.install(pkg)
            }
        }
    }

    private suspend fun extractPyPackages(pyLockFile: PyLockFile, project: Project): Collection<PyPackage> {
        val lockedPackages = pyLockFile.lockedPackages
        val packageByIdentifier = ConcurrentHashMap<LockedPyPackageIdentifier, PyPackage>()

        lockedPackages.parallelForEach { lockedPkg ->
            val repo = PyPackageRepository(lockedPkg.repoMetadata)
            val distUrl = lockedPkg.resolveConcreteDistribution(repo, project)
            val pkg = PyPackage(lockedPkg.name, lockedPkg.version, repo, distUrl)
            checkHashes(pkg, lockedPkg, project)
            packageByIdentifier[lockedPkg.identifier] = pkg
        }

        // Restoring inter-package dependencies via 'comesFrom' field
        for (lockedPkg in lockedPackages) {
            val comesFrom = lockedPkg.comesFrom?.let { packageByIdentifier[it] }
            packageByIdentifier[lockedPkg.identifier]!!.comesFrom = comesFrom
        }

        return packageByIdentifier.values
    }

    private suspend fun checkHashes(pkg: PyPackage, lockedPkg: LockedPyPackage, project: Project) {
        val metadata = PyPackageRepositoryIndexer.downloadMetadata(pkg, project.terminal)
        val availableDistributions = metadata?.releases?.get(pkg.version)

        if (availableDistributions == null && lockedPkg.distributions.isEmpty()) {
            project.terminal.warn("Probably, the corresponding repository ${pkg.repo.url.getSecure()} doesn't contain JSON with metadata needed.")
            // TODO: ask user - trust or not?
            return
        } else if (availableDistributions == null) {
            error("Corresponding locked distribution ${pkg.distributionUrl} was not found in current package metadata. Consider upgrading your lockfile.")
        }

        val currentHash = availableDistributions.find { it.url == pkg.distributionUrl }?.packageHash
        if (currentHash !in lockedPkg.distributions.map { it.hash }) {
            val msg = "Can not find appropriate distribution in the lockfile for ${pkg.distributionUrl}: inconsistent hashes."
            if (lockedPkg.repoMetadata.url.trimmedEquals(PyPackageRepository.PYPI_REPOSITORY.url)) {
                throw Task.ActException(msg)
            } else {
                project.terminal.warn(msg)
                project.terminal.warn(
                    "If repo = ${lockedPkg.repoMetadata.url} is private, then (most probably) " +
                        "the repo owner did not provide package metadata in a JSON format." +
                        "You should consider contact them directly."
                )
            }
        }
    }
}
