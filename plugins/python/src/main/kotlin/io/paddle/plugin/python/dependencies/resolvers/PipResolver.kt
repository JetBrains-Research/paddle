package io.paddle.plugin.python.dependencies.resolvers

import io.paddle.execution.ExecutionResult
import io.paddle.plugin.python.dependencies.index.distributions.ArchivePyDistributionInfo
import io.paddle.plugin.python.dependencies.index.distributions.WheelPyDistributionInfo
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.utils.PyPackageUrl
import io.paddle.plugin.python.utils.trimmedEquals
import io.paddle.project.Project
import io.paddle.tasks.Task
import org.codehaus.plexus.util.cli.CommandLineUtils
import org.codehaus.plexus.util.cli.Commandline
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

    fun resolve(project: Project): Set<PyPackage> {
        val output = ArrayList<String>()

        // Collect requirements which repo is not specified directly (or specified as PyPi)
        val generalRequirements = project.requirements.descriptors
            .filter { it.repo == null || it.repo == PyPackageRepository.PYPI_REPOSITORY.name }
            .map { it.name + (it.version?.let { v -> "==$v" } ?: "") }
        val pipResolveArgs = listOf("-m", "pip", "resolve") + generalRequirements + project.repositories.resolved.asPipArgs

        ExecutionResult(
            CommandLineUtils.executeCommandLine(
                Commandline().apply {
                    executable = project.environment.localInterpreterPath.absolutePathString()
                    addArguments(pipResolveArgs.toTypedArray())
                },
                { output.add(it); project.terminal.stdout(it) },
                { project.terminal.stderr(it) }
            )
        )

        // TODO: resolve requirements which repo is specified directly

        return parse(output, project)
    }

    private fun parse(output: List<String>, project: Project): Set<PyPackage> {
        val startIdx = output.indexOfFirst { it == "--- RESOLVED-BEGIN ---" }
        val endIdx = output.indexOfLast { it == "--- RESOLVED-END ---" }
        if (startIdx == -1 || endIdx == -1) {
            output.map { project.terminal.stderr(it) }
            throw Task.ActException("Package resolution failed.")
        }

        val lines = output.slice((startIdx + 1) until endIdx)
        val comesFromByPackage = HashMap<PyPackage, PyPackageUrl>()

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
                ?: error("FIXME: Unknown distribution type: $filename")

            val repo = project.repositories.resolved.all.find { it.url.trimmedEquals(repoUrl) }
                ?: throw IllegalStateException("Unknown repository: $repoUrl")

            val pkg = PyPackage(name, pyDistributionInfo.version, repo, distributionUrl)
            comesFromByPackage[pkg] = comesFromDistributionUrl
        }

        // Restoring inter-package dependencies via 'comesFrom' field
        for ((pkg, comesFromDistributionUrl) in comesFromByPackage) {
            if (comesFromDistributionUrl != "None") {
                val comesFrom = comesFromByPackage.keys.find { it.distributionUrl == comesFromDistributionUrl }
                pkg.comesFrom = comesFrom
            }
        }

        return comesFromByPackage.keys
    }
}
