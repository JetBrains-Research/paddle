package io.paddle.plugin.python.dependencies.resolvers

import io.paddle.execution.ExecutionResult
import io.paddle.plugin.python.dependencies.index.PyPackagesRepository
import io.paddle.plugin.python.dependencies.index.distributions.ArchivePyDistributionInfo
import io.paddle.plugin.python.dependencies.index.distributions.WheelPyDistributionInfo
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.utils.PyPackageUrl
import io.paddle.plugin.python.utils.trimmedEquals
import io.paddle.project.Project
import org.codehaus.plexus.util.cli.CommandLineUtils
import org.codehaus.plexus.util.cli.Commandline
import kotlin.io.path.absolutePathString

object PipResolver {
    const val PIP_RESOLVER_URL = "https://github.com/SmirnovOleg/pip/releases/download/22.1.dev0-beta/pip_resolver-22.1.dev0-py3-none-any.whl"
    private const val PACKAGE_PROPERTIES_NUM = 7

    fun resolve(project: Project): Set<PyPackage> {
        val output = ArrayList<String>()

        // Collect requirements which repo is not specified directly (or specified as PyPi)
        val generalRequirements = project.requirements.descriptors
            .filter { it.repo == null || it.repo == PyPackagesRepository.PYPI_REPOSITORY.name }
            .map { it.name + (it.version?.let { v -> "==$v" }) }
        val pipResolveArgs = listOf("-m", "pip", "resolve") + generalRequirements + project.repositories.resolved.asPipArgs

        ExecutionResult(
            CommandLineUtils.executeCommandLine(
                Commandline().apply {
                    executable = project.environment.localInterpreterPath.absolutePathString()
                    addArguments(pipResolveArgs.toTypedArray())
                },
                { output.add(it) },
                { project.terminal.stderr(it) }
            )
        )

        return parse(output, project)
    }

    private fun parse(output: List<String>, project: Project): Set<PyPackage> {
        val startIdx = output.indexOfFirst { it == "--- RESOLVED-BEGIN ---" }
        val endIdx = output.indexOfLast { it == "--- RESOLVED-END ---" }
        if (startIdx == -1 || endIdx == -1) {
            project.terminal.error("Package resolution failed.")
            output.map { project.terminal.stderr(it) }
            return emptySet()
        }

        val lines = output.slice((startIdx + 1) until endIdx)
        val parentUrlByPackage = HashMap<PyPackage, PyPackageUrl>()

        // TODO: what if lines are empty?

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
            parentUrlByPackage[pkg] = comesFromDistributionUrl
        }

        // Restoring inter-package dependencies via 'comesFrom' field
        for ((pkg, comesFromDistributionUrl) in parentUrlByPackage) {
            if (comesFromDistributionUrl != "None") {
                val comesFrom = parentUrlByPackage.keys.find { it.distributionUrl == comesFromDistributionUrl }
                pkg.comesFrom = comesFrom
            }
        }

        return parentUrlByPackage.keys
    }
}
