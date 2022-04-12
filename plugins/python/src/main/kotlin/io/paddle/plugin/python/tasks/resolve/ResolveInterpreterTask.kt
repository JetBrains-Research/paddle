package io.paddle.plugin.python.tasks.resolve

import io.paddle.plugin.python.extensions.interpreter
import io.paddle.plugin.python.hasPython
import io.paddle.plugin.python.tasks.PythonPluginTaskGroups
import io.paddle.plugin.standard.extensions.route
import io.paddle.plugin.standard.extensions.subprojects
import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.tasks.incremental.IncrementalTask
import kotlin.system.measureTimeMillis

class ResolveInterpreterTask(project: Project) : IncrementalTask(project) {
    override val id: String = "resolveInterpreter"

    override val group: String = PythonPluginTaskGroups.RESOLVE

    override val dependencies: List<Task>
        get() = project.subprojects.getAllTasksById(this.id)

    override fun act() {
        project.terminal.info("Resolving interpreter...")
        val duration = measureTimeMillis {
            checkInterpreterCompatibility()
            project.interpreter.resolved
        }
        project.terminal.info("Finished: ${duration}ms")
    }

    private fun checkInterpreterCompatibility() {
        if (!project.parents.all { it.hasPython }) {
            return
        }
        for (parent in project.parents) {
            if (parent.interpreter.pythonVersion != project.interpreter.pythonVersion) {
                throw ActException(
                    "${parent.interpreter.pythonVersion.fullName} from ${":" + parent.route.joinToString(":")} " +
                        "is not compatible with ${project.interpreter.pythonVersion.fullName} from ${":" + project.route.joinToString(":")}"
                )
            }
        }
    }
}
