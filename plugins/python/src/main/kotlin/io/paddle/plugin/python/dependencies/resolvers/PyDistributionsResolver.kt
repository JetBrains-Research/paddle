package io.paddle.plugin.python.dependencies.resolvers

import io.paddle.plugin.python.dependencies.index.distributions.ArchivePyDistributionInfo
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.dependencies.index.distributions.WheelPyDistributionInfo
import io.paddle.plugin.python.dependencies.index.webIndexer
import io.paddle.plugin.python.dependencies.packages.PyPackageVersion
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.extensions.globalInterpreter
import io.paddle.plugin.python.extensions.pyLocations
import io.paddle.plugin.python.extensions.repositories
import io.paddle.plugin.python.utils.PyPackageName
import io.paddle.plugin.python.utils.PyPackageUrl
import io.paddle.plugin.python.utils.cached
import io.paddle.plugin.python.utils.getSecure
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.utils.hash.hashable
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer


object PyDistributionsResolver {
    // See https://www.python.org/dev/peps/pep-0425/#id1
    // https://docs.python.org/3/distutils/apiref.html#distutils.util.get_platform
    suspend fun resolve(
        name: PyPackageName,
        version: PyPackageVersion,
        repository: PyPackageRepository,
        project: PaddleProject
    ): PyPackageUrl? =
        cached(
            storage = project.pyLocations.distResolverCachePath.toFile(),
            serializer = MapSerializer(String.serializer(), String.serializer())
        ) {
            val cacheInput = (listOf(name, version).map { it.hashable() } + repository.metadata).hashable().hash()
            getFromCache(cacheInput)?.let { return@cached it }

            val distributions = runBlocking {
                project.webIndexer.downloadDistributionsList(name, repository)
                    .filter { it.version == version }
            }
            val wheels = distributions.filterIsInstance<WheelPyDistributionInfo>()

            // Building candidates for current Python interpreter
            val pyTags = project.globalInterpreter.resolved.version.pep425candidates

            // Building candidates for ABI tag: add "d", "m", or "u" suffix (flag) to each interpreter
            val abiTags = pyTags.map { listOf(it, it + "m", it + "d", it + "u") }.flatten() + listOf("none", "abi3")

            // Intersecting sets of available platforms with current platform
            val platformTags = wheels.map { it.platformTag }.toSet()
            platformTags.asSequence()
                .map { it.split(".") }.flatten() // splitting compressed tags
                .filter { project.executor.os.familyPep425 in it }
                .filter { project.executor.os.archPep425 in it || "universal" in it || "86" in project.executor.os.archPep425 && "intel" in it }
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
                    .mapNotNull { platformTag -> platformTags.indexOf(platformTag).takeIf { it >= 0 } }.minOrNull()
                    ?: Int.MAX_VALUE
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
                ?: return@cached null

            val distributionUrl =
                runBlocking { project.webIndexer.getDistributionUrl(matchedDistributionInfo, repository) }
                    ?: throw Task.ActException(
                        "Distribution ${matchedDistributionInfo.distributionFilename} " +
                                "was not found in the repository ${repository.url.getSecure()}"
                    )
            updateCache(cacheInput, distributionUrl)

            return@cached distributionUrl
        }

    suspend fun resolve(
        name: PyPackageName,
        version: PyPackageVersion,
        project: PaddleProject
    ): Pair<PyPackageUrl, PyPackageRepository> {
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
        throw Task.ActException("Could not resolve $name:$version within specified set of repositories.")
    }
}
