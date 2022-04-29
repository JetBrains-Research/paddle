package io.paddle.plugin.python.tasks.setup

import io.paddle.plugin.python.extensions.buildEnvironment
import io.paddle.plugin.standard.extensions.roots
import io.paddle.plugin.standard.tasks.clean
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import io.paddle.utils.tasks.TaskDefaultGroups
import kotlin.system.measureTimeMillis

class BuildTask(project: PaddleProject) : IncrementalTask(project) {
    override val id: String = "build"

    override val group: String = TaskDefaultGroups.BUILD

    override val inputs: List<Hashable>
        get() = project.roots.sources.map { it.hashable() } + project.buildEnvironment
    override val outputs: List<Hashable>
        get() = listOf(project.workDir.resolve("build").hashable())

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("resolveInterpreter")) +
            project.subprojects.getAllTasksById(this.id)

    override fun initialize() {
        project.tasks.clean.locations.add(project.buildEnvironment.distDir)
        val eggInfos = project.roots.sources.flatMap {
            it.listFiles { entry -> entry.name.endsWith(".egg-info") }
                ?.toList()
                ?: emptyList()
        }
        for (eggInfo in eggInfos) {
            project.tasks.clean.locations.add(eggInfo)
        }
    }

    override fun act() {
        project.terminal.info("Building package...")
        val duration = measureTimeMillis {
            project.buildEnvironment.build().orElse { throw ActException("Build has failed.") }
        }
        project.terminal.info("Finished: ${duration}ms")
    }
}
