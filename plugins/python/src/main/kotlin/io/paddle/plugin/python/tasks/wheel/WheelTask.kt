package io.paddle.plugin.python.tasks.wheel

import io.paddle.execution.ExecutionResult
import io.paddle.plugin.python.dependencies.setup.SetupConfig
import io.paddle.plugin.python.extensions.BuildEnvironment
import io.paddle.plugin.python.extensions.buildEnvironment
import io.paddle.plugin.python.extensions.environment
import io.paddle.plugin.python.extensions.metadata
import io.paddle.plugin.standard.extensions.roots
import io.paddle.plugin.standard.tasks.clean
import io.paddle.project.PaddleProject
import io.paddle.project.extensions.descriptor
import io.paddle.project.extensions.routeAsString
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import io.paddle.utils.tasks.TaskDefaultGroups
import kotlin.io.path.absolutePathString
import kotlin.system.measureTimeMillis

class WheelTask(project: PaddleProject) : IncrementalTask(project) {
    override val id: String = "wheel"

    override val group: String = TaskDefaultGroups.BUILD

    override val inputs: List<Hashable>
        get() = listOf(project.roots.sources.hashable(), project.buildEnvironment, project.descriptor, project.metadata)
    override val outputs: List<Hashable>
        get() = listOf(project.roots.dist.hashable())

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("install")) + project.subprojects.getAllTasksById(this.id)

    override fun initialize() {
        project.tasks.clean.locations.add(project.roots.dist)
        val eggInfos = project.roots.sources.listFiles { entry -> entry.name.endsWith(".egg-info") }?.toList()
            ?: emptyList()
        for (eggInfo in eggInfos) {
            project.tasks.clean.locations.add(eggInfo)
        }
    }

    override fun act() {
        if (!project.roots.sources.exists()) {
            project.terminal.warn("${project.routeAsString} does not contain source root. Skipping...")
            return
        }

        project.terminal.info("Building package...")
        val duration = measureTimeMillis {
            build(project.buildEnvironment).orElse { throw ActException("Build has failed.") }
        }
        project.terminal.info("Finished: ${duration}ms")
    }

    private fun build(buildEnv: BuildEnvironment): ExecutionResult {
        if (!buildEnv.pyprojectToml.exists()) {
            project.terminal.info("Creating ${buildEnv.pyprojectToml.relativeTo(project.workDir).path} file...")
            buildEnv.pyprojectToml.createNewFile()
            buildEnv.pyprojectToml.writeText(
                listOf(
                    "[build-system]",
                    "requires = [\"setuptools>=61.0\"]",
                    "build-backend = \"setuptools.build_meta\"",
                ).joinToString("\n")
            )
        }

        if (!buildEnv.setupCfg.exists()) {
            project.terminal.info("Creating ${buildEnv.setupCfg.relativeTo(project.workDir).path} file...")
            SetupConfig(project).also { it.create(buildEnv.setupCfg) }
        }

        return project.executor.execute(
            project.environment.localInterpreterPath.absolutePathString(),
            listOf("-m", "pip", "install", "--upgrade", "build"),
            project.workDir,
            project.terminal
        ).then {
            project.executor.execute(
                project.environment.localInterpreterPath.absolutePathString(),
                listOf("-m", "build", "--outdir", project.roots.dist.absolutePath),
                project.workDir,
                project.terminal
            )
        }
    }
}
