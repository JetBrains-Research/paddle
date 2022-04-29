package io.paddle.plugin.python.extensions

import io.paddle.execution.ExecutionResult
import io.paddle.plugin.python.dependencies.GlobalCacheRepository
import io.paddle.plugin.python.dependencies.VenvDir
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.dependencies.resolvers.PipResolver
import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.PaddleProject
import io.paddle.terminal.Terminal
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString


val PaddleProject.environment: Environment
    get() = extensions.get(Environment.Extension.key)!!

class Environment(val project: PaddleProject, val venv: VenvDir) : Hashable {

    val interpreterPath: Path
        get() = venv.getInterpreterPath(project)

    val pythonPath: String
        get() {
            val paths = project.roots.sources.map { it.canonicalPath } + project.subprojects.map { it.environment.pythonPath }
            return paths.joinToString(System.getProperty("path.separator"))
        }

    object Extension : PaddleProject.Extension<Environment> {
        override val key: Extendable.Key<Environment> = Extendable.Key()

        override fun create(project: PaddleProject): Environment {
            val config = object : ConfigurationView("environment", project.config) {
                val venv by string("path", default = ".venv")
            }

            return Environment(project, VenvDir(File(project.workDir, config.venv)))
        }
    }

    fun initialize(): ExecutionResult {
        return project.executor.execute(
            project.interpreter.resolved.path.toString(),
            listOf("-m", "venv", venv.absolutePath),
            project.workDir,
            project.terminal
        ).then {
            project.executor.execute(
                interpreterPath.absolutePathString(),
                listOf("-m", "pip", "install", PipResolver.PIP_RESOLVER_URL),
                project.workDir,
                Terminal.MOCK
            )
        }
    }

    fun runModule(module: String, arguments: List<String> = emptyList()): ExecutionResult {
        return project.executor.execute(
            interpreterPath.absolutePathString(),
            listOf("-m", module, *arguments.toTypedArray()),
            project.workDir,
            project.terminal,
            hashMapOf("PYTHONPATH" to project.environment.pythonPath)
        )
    }

    fun runScript(file: String, arguments: List<String> = emptyList()): ExecutionResult {
        return project.executor.execute(
            interpreterPath.absolutePathString(),
            listOf(file, *arguments.toTypedArray()),
            project.workDir,
            project.terminal,
            hashMapOf("PYTHONPATH" to project.environment.pythonPath)
        )
    }

    fun install(pkg: PyPackage) {
        if (venv.hasInstalledPackage(pkg)) return
        val cachedPkg = GlobalCacheRepository.findPackage(pkg, project)
        GlobalCacheRepository.createSymlinkToPackage(cachedPkg, venv)
    }

    override fun hash(): String {
        return venv.hashable().hash()
    }
}
