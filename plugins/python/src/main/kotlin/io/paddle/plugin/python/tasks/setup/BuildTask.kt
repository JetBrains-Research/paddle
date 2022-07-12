package io.paddle.plugin.python.tasks.setup

import io.paddle.execution.ExecutionResult
import io.paddle.plugin.python.dependencies.setup.SetupConfig
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.standard.extensions.roots
import io.paddle.plugin.standard.tasks.clean
import io.paddle.project.PaddleProject
import io.paddle.project.extensions.descriptor
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import io.paddle.utils.tasks.TaskDefaultGroups
import kotlin.io.path.absolutePathString
import kotlin.system.measureTimeMillis

class BuildTask(project: PaddleProject) : IncrementalTask(project) {
    override val id: String = "build"

    override val group: String = TaskDefaultGroups.BUILD

    override val inputs: List<Hashable>
        get() = listOf(project.roots.sources.hashable(), project.buildEnvironment, project.descriptor, project.metadata)
    override val outputs: List<Hashable>
        get() = listOf(project.roots.dist.hashable())

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("install")) + project.subprojects.getAllTasksById(this.id)

    override fun initialize() {
        project.tasks.clean.locations.add(project.roots.dist)
        val eggInfos = project.roots.sources.listFiles { entry -> entry.name.endsWith(".egg-info") }?.toList() ?: emptyList()
        for (eggInfo in eggInfos) {
            project.tasks.clean.locations.add(eggInfo)
        }
    }

    override fun act() {
        project.terminal.info("Building package...")
        val duration = measureTimeMillis {
            build(project.buildEnvironment).orElse { throw ActException("Build has failed.") }
        }
        project.terminal.info("Finished: ${duration}ms")
    }

    private fun build(buildEnv: BuildEnvironment): ExecutionResult {
        project.terminal.info("Creating ${buildEnv.pyprojectToml.relativeTo(project.workDir).path} file...")
        buildEnv.pyprojectToml.createNewFile()
        buildEnv.pyprojectToml.writeText(
            listOf(
                "[build-system]",
                "requires = [\"setuptools>=61.0\"]",
                "build-backend = \"setuptools.build_meta\"",
            ).joinToString("\n")
        )

        project.terminal.info("Creating ${buildEnv.setupCfg.relativeTo(project.workDir).path} file...")
        SetupConfig(project).also { it.create(buildEnv.setupCfg) }

        return project.executor.execute(
            project.environment.interpreterPath.absolutePathString(),
            listOf("-m", "pip", "install", "--upgrade", "build"),
            project.workDir,
            project.terminal
        ).then {
            project.executor.execute(
                project.environment.interpreterPath.absolutePathString(),
                listOf("-m", "build", "--outdir", project.roots.dist.absolutePath),
                project.workDir,
                project.terminal
            )
        }
    }
}
