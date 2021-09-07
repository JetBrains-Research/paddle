package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.dependencies.GlobalCacheRepository
import io.paddle.plugin.python.dependencies.VenvDir
import io.paddle.project.Project
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import java.io.File
import java.nio.file.Files


val Project.environment: Environment
    get() = extensions.get(Environment.Extension.key)!!

class Environment(val project: Project, val venv: VenvDir, val workDir: File) {
    object Extension : Project.Extension<Environment> {
        override val key: Extendable.Key<Environment> = Extendable.Key()

        override fun create(project: Project): Environment {
            val config = object : ConfigurationView("environment", project.config) {
                val venv by string("path", default = ".venv")
            }

            return Environment(project, VenvDir(File(project.workDir, config.venv)), project.workDir)
        }
    }

    fun initialize(): Int {
        return project.executor.execute("python3", listOf("-m", "venv", venv.absolutePath), workDir, project.terminal)
    }

    fun runModule(module: String, arguments: List<String> = emptyList()): Int {
        return project.executor.execute("${venv.absolutePath}/bin/python", listOf("-m", module, *arguments.toTypedArray()), workDir, project.terminal)
    }

    fun runScript(file: String, arguments: List<String> = emptyList()): Int {
        return project.executor.execute("${venv.absolutePath}/bin/python", listOf(file, *arguments.toTypedArray()), workDir, project.terminal)
    }

    fun install(dependency: Requirements.Descriptor): Int {
        if (!GlobalCacheRepository.hasCached(dependency)) {
            val code = GlobalCacheRepository.install(dependency)
            if (code != 0) {
                return code
            }
        }
        val targetPackageGlobalPath = GlobalCacheRepository.getPathToDependency(dependency)
        val localPackageLink = venv.sitePackages.resolve(dependency.name)
        if (!localPackageLink.exists()) {
            // TODO: I guess, we should symlink <package>-<version>.dist-info folder as well
            Files.createSymbolicLink(localPackageLink.toPath(), targetPackageGlobalPath)
        }
        return 0
    }

    fun install(requirements: File): Int {
        // TODO: use paddle-global-cache pip by default
        return project.executor.execute("${venv.absolutePath}/bin/pip", listOf("install", "-r", requirements.absolutePath), workDir, project.terminal)
    }
}
