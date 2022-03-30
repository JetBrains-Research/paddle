package io.paddle.plugin.python.tasks.resolve

import io.paddle.plugin.python.PaddlePyConfig
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.extensions.repositories
import io.paddle.plugin.python.tasks.PythonPluginTaskGroups
import io.paddle.project.Project
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.system.measureTimeMillis

class ResolveRepositoriesTask(project: Project) : IncrementalTask(project) {
    override val id: String = "resolveRepositories"

    override val group: String = PythonPluginTaskGroups.RESOLVE

    // Inputs: current configuration in the paddle.yaml for repositories descriptors (e.g., url + name + default + secondary)
    override val inputs: List<Hashable> = listOf(project.repositories)

    // Outputs: checksums for file indexes of the repositories which are specified in the current project
    override val outputs: List<Hashable>
        get() {
            val descriptors = project.repositories.descriptors.map { PyPackageRepository(it.url, it.name) }
            return PaddlePyConfig.indexDir.listDirectoryEntries()
                .filter { descriptors.any { desc -> desc.cacheFileName == it.name } }
                .map { it.toFile().hashable() }
        }

    override fun act() {
        project.terminal.info("Resolving and indexing repositories...")
        val duration = measureTimeMillis { project.repositories.resolved }
        project.terminal.info("Finished: ${duration}ms")
    }
}
