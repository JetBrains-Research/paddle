package io.paddle.plugin.python.env

import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import io.paddle.utils.Hashable
import io.paddle.utils.hashable

class VenvTask(project: Project) : IncrementalTask(project) {
    companion object {
        private val default = listOf(
            "wheel",
            "pytest",
            "mypy",
            "pylint"
        )
    }

    override val id: String = "venv"

    override val inputs: List<Hashable> = listOf(project.requirements)
    override val outputs: List<Hashable> = listOf(project.environment.venv.hashable())

    override fun act() {
        var code = project.environment.initialize()
        if (code != 0) throw ActException("VirtualEnv creation has failed")

        for (file in project.requirements.files) {
            code = project.environment.install(file)
            if (code != 0) throw ActException("Requirements.txt installation has failed")
        }

        for (pkg in project.requirements.descriptors) {
            code = project.environment.install(pkg)
            if (code != 0) throw ActException("$pkg installation has failed")
        }
    }
}
