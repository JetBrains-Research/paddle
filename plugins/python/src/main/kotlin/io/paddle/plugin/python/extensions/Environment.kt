package io.paddle.plugin.python.extensions

import io.paddle.project.Project
import io.paddle.terminal.Terminal
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import java.io.File


val Project.environment: Environment
    get() = extensions.get(Environment.Extension.key)!!

class Environment(val venv: File, val workingDir: File) {
    object Extension : Project.Extension<Environment> {
        override val key: Extendable.Key<Environment> = Extendable.Key()

        override fun create(project: Project): Environment {
            val config = object : ConfigurationView("environment", project.config) {
                val venv by string("virtualenv", default = ".venv")
            }

            return Environment(File(config.venv), File("."))
        }
    }

    fun initialize(): Int {
        return Terminal.execute("python3", listOf("-m", "venv", venv.absolutePath), workingDir)
    }

    fun runModule(module: String, arguments: List<String> = emptyList()): Int {
        return Terminal.execute("${venv.absolutePath}/bin/python", listOf("-m", module, *arguments.toTypedArray()), workingDir)
    }

    fun runScript(file: String, arguments: List<String> = emptyList()): Int {
        return Terminal.execute("${venv.absolutePath}/bin/python", listOf(file, *arguments.toTypedArray()), workingDir)
    }

    fun install(dependency: Requirements.Descriptor): Int {
        return Terminal.execute("${venv.absolutePath}/bin/pip", listOf("install", "${dependency.id}==${dependency.version}"), workingDir)
    }

    fun install(requirements: File): Int {
        return Terminal.execute("${venv.absolutePath}/bin/pip", listOf("install", "-r", requirements.absolutePath), workingDir)
    }
}
