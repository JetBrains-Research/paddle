package io.paddle.plugin.python.tasks.test

import io.paddle.plugin.python.dependencies.packages.PyPackageVersionSpecifier
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.standard.tasks.clean
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.utils.tasks.TaskDefaultGroups
import java.io.File
import java.nio.file.Paths

class PyTestTask(
    name: String,
    project: PaddleProject,
    val versionSpec: PyPackageVersionSpecifier,
    val targets: List<File>,
    val keywords: String?,
    val additionalArgs: List<String>
) : Task(project) {

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun from(project: PaddleProject): List<PyTestTask> {
            val configurations = project.config.get<List<Map<String, Any>>?>("tasks.pytest") ?: return emptyList()
            val tasks = ArrayList<PyTestTask>()

            for (configuration in configurations) {
                val name = configuration["id"] as String
                val versionSpec = PyPackageVersionSpecifier.fromString(configuration.getOrDefault("version", "7.1.2") as String)
                val relativeTargets = configuration["targets"] as List<String>? ?: emptyList()
                val targets = ArrayList<File>()

                for (relativeTarget in relativeTargets) {
                    if (Paths.get(relativeTarget).isAbsolute) {
                        targets.add(File(relativeTarget))
                    }
                    val absoluteTarget = project.workDir.resolve(relativeTarget).takeIf { it.exists() }
                        ?: throw ActException("Pytest target $relativeTarget was not found at ${project.workDir.absolutePath}")
                    targets.add(absoluteTarget)
                }

                val keywords = (configuration["keywords"] as String?)
                    ?.takeUnless { it.isBlank() }
                val additionalArgs = (configuration["parameters"] as String?)
                    ?.takeUnless { it.isBlank() }
                    ?.split(" ")
                    ?: emptyList()

                tasks.add(PyTestTask(name, project, versionSpec, targets, keywords, additionalArgs))
            }
            return tasks
        }
    }

    override val id: String = "pytest$$name"

    override val group: String = TaskDefaultGroups.TEST

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("install"))

    override fun initialize() {
        project.requirements.descriptors.add(Requirements.Descriptor("pytest", versionSpec))
        project.tasks.clean.locations.add(File(project.workDir, ".pytest_cache"))
    }

    override fun act() {
        if (targets.isEmpty()) {
            project.terminal.error("No pytest targets detected. Stopping...")
            throw ActException("Pytest tests has failed.")
        } else {
            project.terminal.info("Pytest targets detected: $targets")
        }

        val args = additionalArgs.toMutableList()
        keywords?.let { args.apply { add("-k"); add(it) } }
        targets.map { it.absolutePath }.forEach { args.add(it) }

        project.environment.runModule("pytest", args).orElse {
            throw ActException("Pytest tests has failed.")
        }
    }
}
