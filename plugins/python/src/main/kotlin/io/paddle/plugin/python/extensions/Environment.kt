package io.paddle.project

import io.paddle.terminal.Terminal
import io.paddle.utils.config.Configuration
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import java.io.File


object EnvironmentExtension: Project.Extension<Environment> {
    override val key: Extendable.Key<Environment> = Extendable.Key()

    override fun create(project: Project): Environment {
        return Environment.from(project.config)
    }
}

val Project.environment: Environment
    get() = extensions.get(EnvironmentExtension.key)!!

class Environment(val venv: File, val workingDir: File) {
    companion object {
        fun from(configuration: Configuration): Environment {
            val view = ConfigurationView("environment", configuration)
            return Environment(File(view.get("virtualenv") ?: ".venv"), File("."))
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
