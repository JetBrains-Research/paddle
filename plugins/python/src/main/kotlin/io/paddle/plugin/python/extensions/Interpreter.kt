package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.dependencies.PyInterpreter
import io.paddle.project.PaddleProject
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable


val PaddleProject.interpreter: Interpreter
    get() = extensions.get(Interpreter.Extension.key)!!

class Interpreter(val project: PaddleProject, val pythonVersion: PyInterpreter.Version) : Hashable {

    val resolved: PyInterpreter by lazy { PyInterpreter.find(pythonVersion, project) }

    object Extension : PaddleProject.Extension<Interpreter> {
        override val key: Extendable.Key<Interpreter> = Extendable.Key()

        override fun create(project: PaddleProject): Interpreter {
            val config = object : ConfigurationView("environment", project.config) {
                val pythonVersion by version("python", default = "3.8")
            }

            return Interpreter(project, PyInterpreter.Version(config.pythonVersion))
        }
    }

    override fun hash(): String {
        return pythonVersion.number.hashable().hash()
    }
}
