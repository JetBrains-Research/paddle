package io.paddle.plugin.python.dependencies.resolvers

import io.paddle.plugin.python.PaddlePyConfig
import io.paddle.plugin.python.dependencies.index.PyPackageRepositoryIndexer
import io.paddle.plugin.python.dependencies.index.distributions.*
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.extensions.interpreter
import io.paddle.plugin.python.extensions.repositories
import io.paddle.plugin.python.utils.*
import io.paddle.project.Project
import io.paddle.utils.hash.hashable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer


object PyDistributionsResolver {
    // See https://www.python.org/dev/peps/pep-0425/#id1
    // https://docs.python.org/3/distutils/apiref.html#distutils.util.get_platform
    // TODO: caching
    suspend fun resolve(name: PyPackageName, version: PyPackageVersion, repository: PyPackageRepository, project: Project): PyPackageUrl? {
        Cache.find(name, version, repository)?.let { return it }

        val distributions = PyPackageRepositoryIndexer.downloadDistributionsList(name, repository).filter { it.version == version }
        val wheels = distributions.filterIsInstance<WheelPyDistributionInfo>()

        // Building candidates for current Python interpreter
        val pyTags = project.interpreter.resolved.version.pep425candidates

        // Building candidates for ABI tag: add "d", "m", or "u" suffix (flag) to each interpreter
        val abiTags = pyTags.map { listOf(it, it + "m", it + "d", it + "u") }.flatten() + listOf("none", "abi3")

        // Intersecting sets of available platforms with current platform
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

        val distributionUrl = PyPackageRepositoryIndexer.getDistributionUrl(matchedDistributionInfo, repository)
            ?: error("Distribution ${matchedDistributionInfo.distributionFilename} was not found in the repository ${repository.url}")
        Cache.update(name, version, repository, distributionUrl)

        return distributionUrl
    }

    suspend fun resolve(name: PyPackageName, version: PyPackageVersion, project: Project): Pair<PyPackageUrl, PyPackageRepository> {
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

    object Cache {
        private val storage = PaddlePyConfig.distResolverCachePath.toFile()

        private var cache: Map<String, PyPackageUrl>
            get() {
                storage.parentFile.mkdirs()
                return storage.takeIf { it.exists() }
                    ?.let { jsonParser.decodeFromString(MapSerializer(String.serializer(), String.serializer()), it.readText()) }
                    ?: emptyMap()
            }
            set(value) {
                storage.parentFile.mkdirs()
                storage.writeText(jsonParser.encodeToString(MapSerializer(String.serializer(), String.serializer()), value))
            }

        fun find(name: PyPackageName, version: PyPackageVersion, repository: PyPackageRepository): PyPackageUrl? {
            val input = listOf(name.hashable(), version.hashable(), repository.url.hashable()).hashable().hash()
            return cache[input]
        }

        @Synchronized
        fun update(name: PyPackageName, version: PyPackageVersion, repository: PyPackageRepository, distributionUrl: PyPackageUrl) {
            val input = listOf(name.hashable(), version.hashable(), repository.url.hashable()).hashable().hash()
            cache = cache.toMutableMap().also { it[input] = distributionUrl }
        }
    }
}
