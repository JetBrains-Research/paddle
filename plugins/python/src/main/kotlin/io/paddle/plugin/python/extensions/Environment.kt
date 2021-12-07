package io.paddle.plugin.python.extensions

import io.paddle.execution.ExecutionResult
import io.paddle.plugin.python.dependencies.GlobalCacheRepository
import io.paddle.plugin.python.dependencies.VenvDir
import io.paddle.plugin.python.dependencies.index.PyPackagesRepositories
import io.paddle.project.Project
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import java.io.File


val Project.environment: Environment
    get() = extensions.get(Environment.Extension.key)!!

class Environment(val project: Project, val interpreter: PythonTag, val venv: VenvDir, val workDir: File) {
    object Extension : Project.Extension<Environment> {
        override val key: Extendable.Key<Environment> = Extendable.Key()

        override fun create(project: Project): Environment {
            val config = object : ConfigurationView("environment", project.config) {
                val pythonTag by string("python", default = "py3")
                val venv by string("path", default = ".venv")
            }

            return Environment(
                project = project,
                interpreter = PythonTag.valueOf(config.pythonTag.uppercase()),
                venv = VenvDir(File(project.workDir, config.venv)),
                workDir = project.workDir
            )
        }
    }

    enum class PythonTag { PY2, PY3 }

    fun initialize(): ExecutionResult {
        return project.executor.execute("python3", listOf("-m", "venv", venv.absolutePath), workDir, project.terminal)
    }

    fun runModule(module: String, arguments: List<String> = emptyList()): ExecutionResult {
        return project.executor.execute("${venv.absolutePath}/bin/python", listOf("-m", module, *arguments.toTypedArray()), workDir, project.terminal)
    }

    fun runScript(file: String, arguments: List<String> = emptyList()): ExecutionResult {
        return project.executor.execute("${venv.absolutePath}/bin/python", listOf(file, *arguments.toTypedArray()), workDir, project.terminal)
    }

    fun install(descriptor: Requirements.Descriptor, repositories: PyPackagesRepositories) {
        if (venv.hasInstalledPackage(descriptor)) return
        val pkg = GlobalCacheRepository.findPackage(descriptor, repositories)
        GlobalCacheRepository.createSymlinkToPackageRecursively(pkg, symlinkDir = venv.sitePackages.toPath())
    }
}
