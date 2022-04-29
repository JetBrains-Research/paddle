package io.paddle.plugin.migration.tasks.migrate

import io.paddle.plugin.migration.RequirementsTxt
import io.paddle.plugin.migration.utils.collectFiles
import io.paddle.project.PaddleProject
import io.paddle.project.PaddleProjectProvider
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.config.ConfigurationYAML
import io.paddle.utils.hash.*
import kotlin.system.measureTimeMillis

class ParseRequirementsTxtTask(project: PaddleProject) : IncrementalTask(project) {
    override val id: String = "parseRequirementsTxt"

    override val group: String = "migrate"

    override val inputs: List<Hashable>
        get() = listOf(AggregatedHashable(project.workDir.collectFiles("requirements.txt").map { it.hashable() }.toList()))
    override val outputs: List<Hashable>
        get() = listOf(AggregatedHashable(project.workDir.collectFiles("paddle.yaml").map { it.hashable() }.toList()))

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("createDefaultBuildFiles"))

    override fun act() {
        project.terminal.info("Parsing requirements.txt files...")
        val duration = measureTimeMillis {
            val provider = PaddleProjectProvider.getInstance(project.rootDir)
            provider.sync()
            project.rootDir.walkTopDown()
                .filter { it.name == "paddle.yaml" && it.parentFile.resolve("requirements.txt").exists() }
                .forEach {
                    val subproject = provider.getProject(it.parentFile)
                        ?: throw ActException("Could not find project in ${it.canonicalPath}")
                    val requirementsTxt = RequirementsTxt(subproject)
                    requirementsTxt.file
                        ?: throw ActException("requirements.txt file was not found in the project working directory: ${project.workDir}")
                    requirementsTxt.updateDefaultBuildFile(subproject.config as ConfigurationYAML)
                }
        }
        project.terminal.info("Finished: ${duration}ms")
    }
}
