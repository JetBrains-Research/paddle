package io.paddle.plugin.python.extensions

import io.paddle.execution.ExecutionResult
import io.paddle.plugin.python.dependencies.*
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.dependencies.resolvers.PipResolver
import io.paddle.project.Project
import io.paddle.terminal.Terminal
import io.paddle.utils.Hashable
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hashable
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString


val Project.environment: Environment
    get() = extensions.get(Environment.Extension.key)!!

class Environment(val project: Project, val pythonVersion: PyInterpreter.Version, val venv: VenvDir) : Hashable {

    val interpreter: PyInterpreter by lazy { PyInterpreter.find(pythonVersion, project) }

    val localInterpreterPath: Path
        get() = venv.getInterpreterPath(project)

    object Extension : Project.Extension<Environment> {
        override val key: Extendable.Key<Environment> = Extendable.Key()

        override fun create(project: Project): Environment = runBlocking {
            val config = object : ConfigurationView("environment", project.config) {
                val pythonVersion by version("python", default = "3.8")
                val venv by string("path", default = ".venv")
            }

            return@runBlocking Environment(
                project = project,
                pythonVersion = PyInterpreter.Version(config.pythonVersion),
                venv = VenvDir(File(project.workDir, config.venv)),
            )
        }
    }

    fun initialize(): ExecutionResult {
        return project.executor.execute(
            interpreter.path.toString(),
            listOf("-m", "venv", venv.absolutePath),
            project.workDir,
            project.terminal
        ).then {
            project.executor.execute(
                localInterpreterPath.absolutePathString(),
                listOf("-m", "pip", "install", PipResolver.PIP_RESOLVER_URL),
                project.workDir,
                Terminal.MOCK
            )
        }
    }

    fun runModule(module: String, arguments: List<String> = emptyList()): ExecutionResult {
        return project.executor.execute(
            localInterpreterPath.absolutePathString(),
            listOf("-m", module, *arguments.toTypedArray()),
            project.workDir,
            project.terminal
        )
    }

    fun runScript(file: String, arguments: List<String> = emptyList()): ExecutionResult {
        return project.executor.execute(
            localInterpreterPath.absolutePathString(),
            listOf(file, *arguments.toTypedArray()),
            project.workDir,
            project.terminal
        )
    }

    fun install(pkg: PyPackage) {
        if (venv.hasInstalledPackage(pkg)) return
        val cachedPkg = GlobalCacheRepository.findPackage(pkg, project)
        GlobalCacheRepository.createSymlinkToPackage(cachedPkg, venv)
    }

    override fun hash(): String {
        return listOf(pythonVersion.number.hashable(), venv.hashable()).hashable().hash()
    }
}
