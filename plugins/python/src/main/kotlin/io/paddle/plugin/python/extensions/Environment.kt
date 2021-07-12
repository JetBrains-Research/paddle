package io.paddle.plugin.python.extensions

import io.paddle.project.Project
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import java.io.File


val Project.environment: Environment
    get() = extensions.get(Environment.Extension.key)!!

class Environment(val project: Project, val venv: File, val workingDir: File) {
    object Extension : Project.Extension<Environment> {
        override val key: Extendable.Key<Environment> = Extendable.Key()

        override fun create(project: Project): Environment {
            val config = object : ConfigurationView("environment", project.config) {
                val venv by string("path", default = ".venv")
            }

            return Environment(project, File(project.workDir, config.venv), project.workDir)
        }
    }

    fun initialize(): Int {
        return project.executor.execute("python3", listOf("-m", "venv", venv.absolutePath), workingDir, project.terminal)
    }

    fun runModule(module: String, arguments: List<String> = emptyList()): Int {
        return project.executor.execute("${venv.absolutePath}/bin/python", listOf("-m", module, *arguments.toTypedArray()), workingDir, project.terminal)
    }

    fun runScript(file: String, arguments: List<String> = emptyList()): Int {
        return project.executor.execute("${venv.absolutePath}/bin/python", listOf(file, *arguments.toTypedArray()), workingDir, project.terminal)
    }

    fun install(dependency: Requirements.Descriptor): Int {
        return project.executor.execute("${venv.absolutePath}/bin/pip", listOf("install", "${dependency.name}==${dependency.version}"), workingDir, project.terminal)
    }

    fun install(requirements: File): Int {
        return project.executor.execute("${venv.absolutePath}/bin/pip", listOf("install", "-r", requirements.absolutePath), workingDir, project.terminal)
    }
}
