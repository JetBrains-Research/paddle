package io.paddle.plugin.python.dependencies.resolvers

import io.paddle.plugin.python.dependencies.index.distributions.ArchivePyDistributionInfo
import io.paddle.plugin.python.dependencies.index.distributions.WheelPyDistributionInfo
import io.paddle.plugin.python.dependencies.index.webIndexer
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepositories
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.utils.*
import io.paddle.project.PaddleProject
import io.paddle.project.extensions.routeAsString
import io.paddle.tasks.Task
import io.paddle.utils.hash.hashable
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.*
import java.io.File
import java.net.URI
import kotlin.io.path.absolutePathString

/**
 * TODO: rework pip-resolver later to provide <repoUrl> for "already satisfied" requirements
 */
object PipResolver {
    const val PIP_RESOLVER_URL = "https://github.com/SmirnovOleg/pip/releases/download/23.0.1/pip_resolver-23.0.1-py3-none-any.whl"
    private const val PACKAGE_PROPERTIES_NUM = 7

    private val SATISFIED_REQUIREMENT_REGEX =
        Regex("^Requirement already satisfied: (?<name>(.*?)?)(==| |<=|>=|<|>|~=|===|!=)(.*)in .*$")

    fun resolve(project: PaddleProject): Set<PyPackage> = cached(
        storage = project.pyLocations.pipResolverCachePath.toFile(),
        serializer = MapSerializer(String.serializer(), SetSerializer(PyPackage.serializer()))
    ) {
        val requirementsAsPipArgs =
            project.requirements.descriptors.map { it.toString() } +
                project.subprojects.flatMap { subproject -> subproject.requirements.resolved.map { it.toString() } }
        val pipResolveArgs = PipArgs.build("resolve") {
            noCacheDir = project.pythonRegistry.noCacheDir
            packages = requirementsAsPipArgs
            additionalArgs = project.repositories.resolved.asPipArgs
            noBinaryList = project.requirements.descriptors.filter { it.isNoBinary }.map { it.name }
            noIndex = project.environment.noIndex
        }.args
        val executable = project.environment.localInterpreterPath.absolutePathString()
        val input = (pipResolveArgs + executable).map { it.hashable() }.hashable().hash()

        return@cached getFromCache(input) ?: run {
            val output = doResolve(project, executable, pipResolveArgs.toTypedArray())
            val packages = parse(output, project)
            updateCache(input, packages)
            packages
        }
    }

    private fun doResolve(project: PaddleProject, executable: String, arguments: Array<String>): List<String> {
        val output = ArrayList<String>()
        return project.executor.execute(
            command = executable,
            args = arguments.toList(),
            workingDir = project.workDir,
            terminal = project.terminal,
            systemOut = { output.add(it); project.terminal.stdout(it) },
            systemErr = { project.terminal.stderr(it) },
            verbose = false
        ).expose(
            onSuccess = { return@expose output },
            onFail = { throw Task.ActException("Package resolution for project ${project.routeAsString} failed with code $it.") }
        )
    }

    private fun parse(output: List<String>, project: PaddleProject): Set<PyPackage> {
        val satisfiedRequirements = HashSet<PyPackage>()
        for (line in output) {
            SATISFIED_REQUIREMENT_REGEX.find(line.trim('\n'))?.let { matchResult ->
                val name = matchResult.groups["name"]?.value
                    ?: throw Task.ActException("Failed to parse pip-resolver output: could not extract package name from $line")
                val pkg = project.environment.venv.findPackageWithNameOrNull(name)
                    ?: throw Task.ActException(
                        "Could not find existing package $name in ${project.environment.venv.path}: " +
                            "most probably, it does not contain PyPackage.json file in its .dist-info folder to be indexed. " +
                            "Please, consider re-installing this package using Paddle."
                    )
                satisfiedRequirements.add(pkg)
            }
        }

        val startIdx = output.indexOfFirst { it == "--- RESOLVED-BEGIN ---" }
        val endIdx = output.indexOfLast { it == "--- RESOLVED-END ---" }
        if (startIdx == -1 || endIdx == -1) {
            if (output.all { it.contains("Requirement already satisfied") }) {
                return emptySet()
            }
            output.map { project.terminal.stderr(it) }
            throw Task.ActException("Package resolution failed.")
        }

        val lines = output.slice((startIdx + 1) until endIdx)
        val comesFromUrlByPackage = HashMap<PyPackage, PyPackageUrl>()

        var retry = false

        for (i in lines.indices step PACKAGE_PROPERTIES_NUM) {
            val name = lines[i].substringAfter(": ")
            // val constraints = lines[i + 1].substringAfter(": ")
            // val fileExtension = lines[i + 2].substringAfter(": ")
            val filename = lines[i + 3].substringAfter(": ")
            val repoUrl = lines[i + 4].substringAfter(": ").substringBeforeLast("/simple/")
            val distributionUrl = lines[i + 5].substringAfter(": ")
            val comesFromDistributionUrl = lines[i + 6].substringAfter(": ")

            val pyDistributionInfo = WheelPyDistributionInfo.fromString(filename)
                ?: ArchivePyDistributionInfo.fromString(filename)
                ?: throw Task.ActException("FIXME: Unknown distribution type: $filename")

            val version = pyDistributionInfo.version
            val findLinkSourceUrl = distributionUrl.findLinkSourceIn(project.repositories.resolved)


            val repo = when {
                findLinkSourceUrl != null -> {
                    project.repositories.resolved.primarySource // FIXME: make this null requires a lot of code work
                }

                repoUrl == "None" -> { // it was resolved as a local file distribution file://...
                    runBlocking {
                        project.webIndexer.getDistributionUrl(
                            pyDistributionInfo,
                            PyPackageRepository.PYPI_REPOSITORY
                        )
                    } // if null, it was not found in the PyPi repo
                        ?: if (project.pythonRegistry.autoRemove) {
                            val localDistribution = File(URI(distributionUrl))
                            if (!localDistribution.exists()) {
                                throw Task.ActException("Failed to delete local distribution $distributionUrl: file not found.")
                            }
                            if (!localDistribution.delete()) {
                                throw Task.ActException("Failed to delete local distribution $distributionUrl. Please, do it manually and re-run the task.")
                            }
                            retry = true
                        } else {
                            if (!project.pythonRegistry.noCacheDir) {
                            throw Task.ActException("Failed to find distribution $filename  in the repository ${PyPackageRepository.PYPI_REPOSITORY.url.getSecure()}")
                        }
                        project.terminal.warn(
                                "Distribution $filename was not found in the repository ${PyPackageRepository.PYPI_REPOSITORY.url.getSecure()}.\n" +
                                    "It is possible that it was resolved from your local cache, " +
                                    "which is deprecated since it is not available online anymore.\n" +
                                    "Please, consider removing $distributionUrl from cache and re-running the task.\n" +
                                    "Or run again with disabled pip cache using `noCacheDir: true`"
                        )
                    }
                PyPackageRepository.PYPI_REPOSITORY
            } else -> {
                    project.repositories.resolved.all.find { it.url.trimmedEquals(repoUrl) }
                        ?: throw IllegalStateException("Unknown repository: $repoUrl")
                }
            }

            val pkg = PyPackage(name, version, repo, distributionUrl, findLinkSource = findLinkSourceUrl)
            comesFromUrlByPackage[pkg] = comesFromDistributionUrl
        }

        if (retry) throw RetrySignal()

        // Restoring inter-package dependencies via 'comesFrom' field
        for ((pkg, comesFromDistributionUrl) in comesFromUrlByPackage) {
            if (comesFromDistributionUrl != "None") {
                val comesFrom = comesFromUrlByPackage.keys.find { it.distributionUrl == comesFromDistributionUrl }
                pkg.comesFrom = comesFrom
            }
        }

        return comesFromUrlByPackage.keys + satisfiedRequirements
    }

    private fun PyPackageUrl.findLinkSourceIn(repositories: PyPackageRepositories): PyUrl? =
        repositories.linkSources.find { findLinksSource -> this.startsWith(findLinksSource) }

    class RetrySignal : Exception()
}
