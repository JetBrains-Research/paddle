package io.paddle.plugin.python.tasks.migrate

import io.paddle.plugin.python.dependencies.migration.RequirementsTxt
import io.paddle.plugin.python.tasks.PythonPluginTaskGroups
import io.paddle.project.Project
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.*
import kotlin.system.measureTimeMillis

class ParseRequirementsTxtTask(project: Project) : IncrementalTask(project) {
    override val id: String = "parseRequirementsTxt"

    override val group: String = PythonPluginTaskGroups.MIGRATE

    override val inputs: List<Hashable> = listOf(RequirementsTxt(project).file?.hashable() ?: EmptyHashable())
    override val outputs: List<Hashable> = listOf(project.buildFile.hashable())

    override fun act() {
        project.terminal.info("Parsing requirements.txt file...")
        val duration = measureTimeMillis {
            val requirementsTxt = RequirementsTxt(project)
            requirementsTxt.file
                ?: throw ActException("requirements.txt file was not found in the project working directory: ${project.workDir}")
            requirementsTxt.createDefaultPaddleYAML(project.yaml)
        }
        project.terminal.info("Finished: ${duration}ms")
    }
}
