package io.paddle.plugin.python.tasks.env

import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.standard.tasks.clean
import io.paddle.project.Project
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.Hashable
import io.paddle.utils.hashable
import io.paddle.utils.tasks.TaskDefaultGroups

class VenvTask(project: Project) : IncrementalTask(project) {
    override val id: String = "venv"

    override val group: String = TaskDefaultGroups.BUILD

    override val inputs: List<Hashable> = listOf(project.requirements)
    override val outputs: List<Hashable> = listOf(project.environment.venv.hashable())

    override fun initialize() {
        project.requirements.descriptors.add(Requirements.Descriptor("wheel", "0.36.2"))
        project.tasks.clean.locations.add(project.environment.venv)
    }

    override fun act() {
        project.environment.initialize().orElse { throw ActException("VirtualEnv creation has failed") }

        for (pkg in project.requirements.descriptors) {
            project.environment.install(pkg, project.requirements.repositories)
        }
    }
}
