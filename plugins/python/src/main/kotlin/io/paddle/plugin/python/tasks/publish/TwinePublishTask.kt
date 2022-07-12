package io.paddle.plugin.python.tasks.publish

import io.paddle.plugin.python.PyDevPackageDefaultVersions
import io.paddle.plugin.python.dependencies.packages.PyPackageVersionSpecifier
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.tasks.PythonPluginTaskGroups
import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.PaddleProject
import io.paddle.project.extensions.routeAsString
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import java.io.File
import kotlin.io.path.absolutePathString

class TwinePublishTask(project: PaddleProject) : IncrementalTask(project) {
    override val id: String = "twine"

    override val group: String = PythonPluginTaskGroups.PUBLISH

    override val inputs: List<Hashable>
        get() = listOf(project.roots.dist.hashable())

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("build")) + project.subprojects.getAllTasksById(this.id)

    override fun initialize() {
        project.requirements.findByName("twine")
            ?: project.requirements.descriptors.add(
                Requirements.Descriptor(
                    name = "twine",
                    versionSpecifier = PyPackageVersionSpecifier.fromString(PyDevPackageDefaultVersions.TWINE)
                )
            )
    }

    override fun act() {
        val repo = project.publishEnvironment.repo
            ?: throw ActException(
                "Could not infer a repository to publish from existing configuration for project ${project.routeAsString}. " +
                    "Please, specify it directly in the section <tasks.publish.repo> of ${project.buildFile.path}"
            )
        // TODO: adjust (all, latest, whl, tar.gz, ... mask)
        val allDistributions = project.roots.dist.absolutePath.trimEnd(File.separatorChar) + File.separator + "*"

        val optionalArgs = mutableListOf<String>().apply {
            if (project.publishEnvironment.twine.skipExisting) add("--skip-existing")
            if (project.publishEnvironment.twine.verbose) add("--verbose")
        }

        project.executor.execute(
            project.environment.interpreterPath.absolutePathString(),
            listOf("-m", "twine", "upload", "--repository-url", repo.uploadUrl, allDistributions) + optionalArgs,
            project.workDir,
            project.terminal,
            mapOf("TWINE_USERNAME" to repo.credentials.login, "TWINE_PASSWORD" to repo.credentials.password)
        ).orElse {
            throw ActException("Twine publishing failed. Exit code: $it")
        }
    }
}
