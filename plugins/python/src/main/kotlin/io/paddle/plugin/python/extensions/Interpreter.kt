package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.dependencies.PyInterpreter
import io.paddle.plugin.python.hasPython
import io.paddle.project.PaddleProject
import io.paddle.project.extensions.route
import io.paddle.tasks.Task
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import kotlin.system.measureTimeMillis


val PaddleProject.interpreter: Interpreter
    get() = extensions.get(Interpreter.Extension.key)!!

class Interpreter(val project: PaddleProject, val pythonVersion: PyInterpreter.Version) : Hashable {

    val resolved: PyInterpreter by lazy {
        project.terminal.info("Resolving interpreter...")
        val result: PyInterpreter
        measureTimeMillis {
            checkInterpreterCompatibility()
            result = PyInterpreter.find(pythonVersion, project)
        }.also {
            project.terminal.info("Finished: $it ms")
        }
        result
    }

    object Extension : PaddleProject.Extension<Interpreter> {
        override val key: Extendable.Key<Interpreter> = Extendable.Key()

        override fun create(project: PaddleProject): Interpreter {
            val config = object : ConfigurationView("environment", project.config) {
                // 3.8 is Double, but 3.8.1 is String
                val pythonVersion: String = try {
                    this.get<String>("python") ?: "3.8"
                } catch (e: ClassCastException) {
                    this.get<Double>("python")?.toString() ?: "3.8"
                }
            }

            return Interpreter(project, PyInterpreter.Version(config.pythonVersion))
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
            if (parent.interpreter.pythonVersion != project.interpreter.pythonVersion) {
                throw Task.ActException(
                    "${parent.interpreter.pythonVersion.fullName} from ${":" + parent.route.joinToString(":")} " +
                        "is not compatible with ${project.interpreter.pythonVersion.fullName} from ${":" + project.route.joinToString(":")}"
                )
            }
        }
    }
}
