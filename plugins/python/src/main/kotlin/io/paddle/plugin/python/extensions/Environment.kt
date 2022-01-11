package io.paddle.plugin.python.extensions

import io.paddle.execution.ExecutionResult
import io.paddle.plugin.python.dependencies.GlobalCacheRepository
import io.paddle.plugin.python.dependencies.VenvDir
import io.paddle.plugin.python.dependencies.index.PyInterpreter
import io.paddle.plugin.python.dependencies.index.PyPackage
import io.paddle.project.Project
import io.paddle.utils.Hashable
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hashable
import kotlinx.coroutines.runBlocking
import java.io.File


val Project.environment: Environment
    get() = extensions.get(Environment.Extension.key)!!

class Environment(val project: Project, val pythonVersion: PyInterpreter.Version, val venv: VenvDir) : Hashable {

    val interpreter: PyInterpreter by lazy { PyInterpreter.find(pythonVersion, project) }

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
            listOf("-m", "pip", "install", "virtualenv"),
            interpreter.path.parent.toFile(),
            project.terminal
        ).then {
            project.executor.execute(
                interpreter.path.toString(),
                listOf("-m", "virtualenv", venv.absolutePath),
                project.workDir,
                project.terminal
            )
        }
    }

    fun runModule(module: String, arguments: List<String> = emptyList()): ExecutionResult {
        return project.executor.execute(
            "${venv.absolutePath}/bin/python",
            listOf("-m", module, *arguments.toTypedArray()),
            project.workDir,
            project.terminal
        )
    }

    fun runScript(file: String, arguments: List<String> = emptyList()): ExecutionResult {
        return project.executor.execute(
            "${venv.absolutePath}/bin/python",
            listOf(file, *arguments.toTypedArray()),
            project.workDir,
            project.terminal
        )
    }

    fun install(pkg: PyPackage) {
        if (venv.hasInstalledPackage(pkg.descriptor)) return
        val cachedPkg = GlobalCacheRepository.findPackage(pkg, project)
        GlobalCacheRepository.createSymlinkToPackageRecursively(cachedPkg, symlinkDir = venv.sitePackages.toPath())
    }

    override fun hash(): String {
        return listOf(pythonVersion.number.hashable(), venv.hashable()).hashable().hash()
    }
}
