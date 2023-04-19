package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.dependencies.interpreter.*
import io.paddle.plugin.python.hasPython
import io.paddle.project.PaddleProject
import io.paddle.project.extensions.routeAsString
import io.paddle.tasks.Task
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable

val PaddleProject.globalInterpreter: Interpreter
    get() = extensions.getOrFail(Interpreter.Extension.key)

class Interpreter(val project: PaddleProject, val pythonVersion: InterpreterVersion) : Hashable {
    val resolved: PyInterpreter by lazy {
        checkInterpreterCompatibility()
        PyInterpreter.find(pythonVersion, project)
    }

    val cachedVersions: Collection<InterpreterVersion>
        get() = AbstractInterpreterDownloader
            .getDownloader(project)
            .findCachedInstallation()
            .map { it.version }

    val localVersions: Collection<InterpreterVersion>
        get() = AbstractInterpreterDownloader
            .getDownloader(project)
            .findLocalInstallation()
            .map { it.version }

    object Extension : PaddleProject.Extension<Interpreter> {
        override val key: Extendable.Key<Interpreter> = Extendable.Key()

        override fun create(project: PaddleProject): Interpreter {
            val config = object : ConfigurationView("environment", project.config) {
                // 3.8 is Double, but 3.8.1 is String
                val pythonVersion: String? = try {
                    this.get<String>("python")
                } catch (e: ClassCastException) {
                    this.get<Double>("python")?.toString()
                }
            }

            return Interpreter(
                project = project,
                pythonVersion = config.pythonVersion?.let { InterpreterVersion(it) }
                    ?: project.parents.firstOrNull { it.hasPython }?.globalInterpreter?.pythonVersion
                    ?: error("<environment.python> is not specified in project ${project.routeAsString} and could not be inferred")
            )
        }
    }

    override fun hash(): String {
        return pythonVersion.number.hashable().hash()
    }

    private fun checkInterpreterCompatibility() {
        if (!project.parents.all { it.hasPython }) {
            return
        }
        for (parent in project.parents) {
            if (parent.globalInterpreter.pythonVersion != project.globalInterpreter.pythonVersion) {
                throw Task.ActException(
                    "${parent.globalInterpreter.pythonVersion.fullName} from ${parent.routeAsString} " +
                        "is not compatible with ${project.globalInterpreter.pythonVersion.fullName} from ${project.routeAsString}"
                )
            }
        }
    }
}
