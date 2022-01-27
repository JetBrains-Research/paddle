package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.dependencies.index.distributions.*
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.utils.*
import io.paddle.project.Project


object PyDistributionsResolver {
    // See https://www.python.org/dev/peps/pep-0425/#id1
    // https://docs.python.org/3/distutils/apiref.html#distutils.util.get_platform
    suspend fun resolve(name: PyPackageName, version: PyPackageVersion, repository: PyPackagesRepository, project: Project): PyPackageUrl? {
        val distributions = PyPackagesRepositoryIndexer.downloadDistributionsList(name, repository).filter { it.version == version }
        val wheels = distributions.filterIsInstance<WheelPyDistributionInfo>()

        // Building candidates for current Python interpreter
        val pyTags = project.environment.interpreter.version.pep425candidates

        // Building candidates for ABI tag: add "d", "m", or "u" suffix (flag) to each interpreter
        val abiTags = pyTags.map { listOf(it, it + "m", it + "d", it + "u") }.flatten() + listOf("none", "abi3")

        // Intersecting the sets of available platforms with current platform
        val platformTags = wheels.map { it.platformTag }.toSet()
        platformTags.asSequence()
            .map { it.split(".") }.flatten() // splitting compressed tags
            .filter { OsUtils.family in it }
            .filter { OsUtils.arch in it || "universal" in it || "86" in OsUtils.arch && "intel" in it }
            .toList() + "any"

        data class Candidate(
            val pyTagRank: Int = Int.MAX_VALUE,
            val abiTagRank: Int = Int.MAX_VALUE,
            val platformTagRank: Int = Int.MAX_VALUE,
            val wheel: PyDistributionInfo? = null
        )

        var bestCandidate = Candidate()
        for (wheel in wheels) {
            val pyTagRank = wheel.requiresPython.split(".")
                .mapNotNull { pyTag -> pyTags.indexOf(pyTag).takeIf { it >= 0 } }.minOrNull() ?: Int.MAX_VALUE
            val abiTagRank = wheel.abiTag.split(".")
                .mapNotNull { abiTag -> abiTags.indexOf(abiTag).takeIf { it >= 0 } }.minOrNull() ?: Int.MAX_VALUE
            val platformTagRank = wheel.platformTag.split(".")
                .mapNotNull { platformTag -> platformTags.indexOf(platformTag).takeIf { it >= 0 } }.minOrNull() ?: Int.MAX_VALUE
            val currentCandidate = Candidate(pyTagRank, abiTagRank, platformTagRank, wheel)

            if (pyTagRank < bestCandidate.pyTagRank) {
                bestCandidate = currentCandidate
            } else if (abiTagRank < bestCandidate.abiTagRank) {
                bestCandidate = currentCandidate
            } else if (platformTagRank < bestCandidate.platformTagRank) {
                bestCandidate = currentCandidate
            }
        }

        val matchedDistributionInfo = bestCandidate.wheel
            ?: distributions.filterIsInstance<ArchivePyDistributionInfo>().firstOrNull()
            ?: return null

        return PyPackagesRepositoryIndexer.getDistributionUrl(matchedDistributionInfo, repository)
    }

    suspend fun resolve(descriptor: Requirements.Descriptor, project: Project): Pair<PyPackageUrl, PyPackagesRepository> {
        val (name, version, _) = descriptor
        val repos = project.repositories.resolved
        val primaryUrl = resolve(name, version, repos.primarySource, project)
        if (primaryUrl != null)
            return primaryUrl to repos.primarySource
        for (repo in repos.extraSources) {
            val extraUrl = resolve(name, version, repo, project)
            if (extraUrl != null) {
                return extraUrl to repo
            }
        }
        error("Could not resolve $name:$version within specified set of repositories.")
    }
}
