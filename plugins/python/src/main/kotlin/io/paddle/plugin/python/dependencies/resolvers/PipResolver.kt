package io.paddle.plugin.python.dependencies.resolvers

import io.paddle.execution.ExecutionResult
import io.paddle.plugin.python.PaddlePythonRegistry
import io.paddle.plugin.python.PyLocations
import io.paddle.plugin.python.dependencies.index.PyPackageRepositoryIndexer
import io.paddle.plugin.python.dependencies.index.distributions.ArchivePyDistributionInfo
import io.paddle.plugin.python.dependencies.index.distributions.WheelPyDistributionInfo
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.utils.*
import io.paddle.project.PaddleProject
import io.paddle.project.extensions.routeAsString
import io.paddle.tasks.Task
import io.paddle.utils.hash.hashable
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.*
import org.codehaus.plexus.util.cli.CommandLineUtils
import org.codehaus.plexus.util.cli.Commandline
import java.io.ByteArrayInputStream
import java.io.File
import java.net.URI
import kotlin.io.path.absolutePathString

/**
 * Note: I turned off caching everywhere here since it seems to be broken for updates.
 * TODO: rework pip-resolver later to provide <repoUrl> for "already satisfied" requirements
 */
object PipResolver {
    const val PIP_RESOLVER_URL = "https://github.com/SmirnovOleg/pip/releases/download/22.2.dev0-beta/pip_resolver-22.2.dev0-py3-none-any.whl"
    private const val PACKAGE_PROPERTIES_NUM = 7

    private val SATISFIED_REQUIREMENT_REGEX = Regex("^Requirement already satisfied: (?<name>(.*?)?)(==| |<=|>=|<|>|~=|===|!=)(.*)in .*$")

    fun resolve(project: PaddleProject): Set<PyPackage> = cached(
        storage = PyLocations.pipResolverCachePath.toFile(),
        serializer = MapSerializer(String.serializer(), SetSerializer(PyPackage.serializer()))
    ) {
        val requirementsAsPipArgs =
            project.requirements.descriptors.map { it.toString() } +
                project.subprojects.flatMap { subproject -> subproject.requirements.resolved.map { it.toString() } }
        val pipResolveArgs = listOf("-m", "pip", "resolve") + requirementsAsPipArgs + project.repositories.resolved.asPipArgs
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
        return ExecutionResult(
            CommandLineUtils.executeCommandLine(
                Commandline().apply {
                    this.executable = executable
                    addArguments(arguments)
                },
                ByteArrayInputStream("\n\n".encodeToByteArray()),
                { output.add(it); project.terminal.stdout(it) },
                { project.terminal.stderr(it) }
            )
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
            var distributionUrl = lines[i + 5].substringAfter(": ")
            val comesFromDistributionUrl = lines[i + 6].substringAfter(": ")

            val pyDistributionInfo = WheelPyDistributionInfo.fromString(filename)
                ?: ArchivePyDistributionInfo.fromString(filename)
                ?: throw Task.ActException("FIXME: Unknown distribution type: $filename")

            val version = pyDistributionInfo.version

            val repo = if (repoUrl == "None") { // it was resolved as a local file distribution file://...
                runBlocking {
                    PyPackageRepositoryIndexer.getDistributionUrl(pyDistributionInfo, PyPackageRepository.PYPI_REPOSITORY)
                } // if null, it was not found in the PyPi repo
                    ?: if (PaddlePythonRegistry.autoRemove) {
                        val localDistribution = File(URI(distributionUrl))
                        if (!localDistribution.exists()) {
                            throw Task.ActException("Failed to delete local distribution $distributionUrl: file not found.")
                        }
                        if (!localDistribution.delete()) {
                            throw Task.ActException("Failed to delete local distribution $distributionUrl. Please, do it manually and re-run the task.")
                        }
                        retry = true
                    } else {
                        throw Task.ActException(
                            "Distribution $filename was not found in the repository ${PyPackageRepository.PYPI_REPOSITORY.url.getSecure()}.\n" +
                                "It is possible that it was resolved from your local cache, " +
                                "which is deprecated since it is not available online anymore.\n" +
                                "Please, consider removing $distributionUrl from cache and re-running the task."
                        )
                    }
                PyPackageRepository.PYPI_REPOSITORY
            } else {
                project.repositories.resolved.all.find { it.url.trimmedEquals(repoUrl) }
                    ?: throw IllegalStateException("Unknown repository: $repoUrl")
            }

            val pkg = PyPackage(name, version, repo, distributionUrl)
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

    class RetrySignal : Exception()
}
