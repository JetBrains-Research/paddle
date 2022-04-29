package io.paddle.plugin.python.dependencies.resolvers

import io.paddle.execution.ExecutionResult
import io.paddle.plugin.python.PyLocations
import io.paddle.plugin.python.dependencies.index.PyPackageRepositoryIndexer
import io.paddle.plugin.python.dependencies.index.distributions.ArchivePyDistributionInfo
import io.paddle.plugin.python.dependencies.index.distributions.WheelPyDistributionInfo
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.utils.*
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.utils.hash.hashable
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.*
import org.codehaus.plexus.util.cli.CommandLineUtils
import org.codehaus.plexus.util.cli.Commandline
import java.io.ByteArrayInputStream
import kotlin.io.path.absolutePathString

object PipResolver {
    const val PIP_RESOLVER_URL = "https://github.com/SmirnovOleg/pip/releases/download/22.1.dev0-beta/pip_resolver-22.1.dev0-py3-none-any.whl"
    private const val PACKAGE_PROPERTIES_NUM = 7

    private val SATISFIED_REQUIREMENT_REGEX = Regex(
        "^Requirement already satisfied: " +
            "(?<name>.*?)" +
            "(==|>=|<=|>|<|~=|===)" +
            ".* (\\(from (?<comesFromName>.*?) (?<comesFromVersion>[\\w\\-_.*+!]+?)\\))? " +
            "\\((?<version>[\\w\\-_.*+!]+?)\\)"
    )

    fun resolve(project: PaddleProject): Set<PyPackage> =
        cached(
            storage = PyLocations.pipResolverCachePath.toFile(),
            serializer = MapSerializer(String.serializer(), ListSerializer(String.serializer()))
        ) {
            // Collect requirements which repo is not specified directly (or specified as PyPi)
            val generalRequirements = project.requirements.descriptors
                .filter { it.repo == null || it.repo == PyPackageRepository.PYPI_REPOSITORY.name }
                .map { it.name + (it.version?.let { v -> "==$v" } ?: "") }
            val pipResolveArgs = listOf("-m", "pip", "resolve") + generalRequirements + project.repositories.resolved.asPipArgs
            val cacheInput = pipResolveArgs.map { it.hashable() }.hashable().hash()

            val output = ArrayList<String>()
            getFromCache(cacheInput)?.let { output.addAll(it) } ?: ExecutionResult(
                CommandLineUtils.executeCommandLine(
                    Commandline().apply {
                        executable = project.environment.interpreterPath.absolutePathString()
                        addArguments(pipResolveArgs.toTypedArray())
                    },
                    ByteArrayInputStream("\n\n".encodeToByteArray()),
                    { output.add(it); project.terminal.stdout(it) },
                    { project.terminal.stderr(it) }
                )
            )

            // TODO: resolve requirements which repo is specified directly

            val packages = parse(output, project)
            if (packages.isNotEmpty()) {
                updateCache(cacheInput, output)
            }

            return@cached packages
        }

    private fun parse(output: List<String>, project: PaddleProject): Set<PyPackage> {
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


            val repo = if (repoUrl == "None") {
                runBlocking {
                    PyPackageRepository.PYPI_REPOSITORY.also {
                        distributionUrl = PyPackageRepositoryIndexer.getDistributionUrl(pyDistributionInfo, it)
                            ?: throw Task.ActException(
                                "Distribution $filename was not found in the repository ${it.url.getSecure()}.\n" +
                                    "It is possible that it was resolved from your local cache, " +
                                    "which is deprecated since it is not available online anymore.\n" +
                                    "Please, consider removing $distributionUrl and re-running the task."
                            )
                    }
                }
            } else {
                project.repositories.resolved.all.find { it.url.trimmedEquals(repoUrl) }
                    ?: throw IllegalStateException("Unknown repository: $repoUrl")
            }

            val pkg = PyPackage(name, pyDistributionInfo.version, repo, distributionUrl)
            comesFromUrlByPackage[pkg] = comesFromDistributionUrl
        }

        // Restoring inter-package dependencies via 'comesFrom' field
        for ((pkg, comesFromDistributionUrl) in comesFromUrlByPackage) {
            if (comesFromDistributionUrl != "None") {
                val comesFrom = comesFromUrlByPackage.keys.find { it.distributionUrl == comesFromDistributionUrl }
                pkg.comesFrom = comesFrom
            }
        }

        return comesFromUrlByPackage.keys
    }
}
