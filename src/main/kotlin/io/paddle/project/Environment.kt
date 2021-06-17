package io.paddle.project

import io.paddle.project.config.Configuration
import io.paddle.terminal.Terminal
import java.io.File

class Environment(val venv: File, val workingDir: File) {

    companion object {
        fun from(configuration: Configuration): Environment {
            return Environment(File(configuration.environment.virtualenv), File("."))
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
