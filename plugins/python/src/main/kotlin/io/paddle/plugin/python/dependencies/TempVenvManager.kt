package io.paddle.plugin.python.dependencies

import io.paddle.execution.ExecutionResult
import io.paddle.plugin.python.PaddlePyConfig
import io.paddle.plugin.python.dependencies.index.PyPackage
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.utils.deepResolve
import io.paddle.project.Project
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * A service for managing Paddle's internal virtual environment, where all the packages are installed for the first time.
 * Then, Paddle moves them to the corresponding ~/.paddle/cache/repo/package/version folder.
 */
class TempVenvManager private constructor(val venv: VenvDir, val project: Project) {
    companion object {
        @Volatile
        private var instance: TempVenvManager? = null

        fun getInstance(project: Project): TempVenvManager =
            instance ?: synchronized(this) {
                instance ?: getGlobalVenvManager(project).also { instance = it }
            }

        private fun getGlobalVenvManager(project: Project): TempVenvManager {
            val venvPath = PaddlePyConfig.venvsDir.resolve(project.id)
            createVenv(project).orElse { error("Failed to create Paddle's internal virtualenv. Check your python installation.") }
            return TempVenvManager(VenvDir(venvPath.toFile()), project)
        }

        private fun createVenv(project: Project, options: List<String> = emptyList(), verbose: Boolean = true): ExecutionResult {
            return project.executor.execute(
                command = project.environment.interpreter.path.toString(),
                args = listOf("-m", "venv") + options + PaddlePyConfig.venvsDir.resolve(project.id).toString(),
                workingDir = PaddlePyConfig.paddleHome.toFile(),
                terminal = project.terminal,
                verbose = verbose
            )
        }
    }

    val interpreterPath: Path
        get() = venv.deepResolve("bin", "python").toPath()

    fun clearInstall(pkg: PyPackage): ExecutionResult {
        return createVenv(project, options = listOf("--clear"), verbose = false).then { install(pkg) }
    }

    private fun install(pkg: PyPackage): ExecutionResult {
        val repos = project.repositories.resolved
        val args = ArrayList<String>().apply {
            add("install")
            add("--index-url")
            add(repos.primarySource.urlSimple)
            for (repo in repos.all) {
                if (repo != repos.primarySource) {
                    add("--extra-index-url")
                    add(repo.urlSimple)
                }
            }
            add("${pkg.name}==${pkg.version}")
        }

        return project.executor.execute(
            command = interpreterPath.absolutePathString(),
            args = listOf("-m", "pip") + args,
            workingDir = PaddlePyConfig.paddleHome.toFile(),
            terminal = project.terminal
        )
    }

    fun contains(pkgName: String): Boolean {
        return venv.sitePackages.listFiles()?.any { it.name.startsWith("$pkgName-") } ?: false
    }

    fun getInstalledPackageVersionByName(pkgName: String): String {
        return InstalledPackageInfo.findByPackageName(venv.sitePackages, pkgName).pkgVersion
    }

    fun getFilesRelatedToPackage(descriptor: Requirements.Descriptor): List<File> {
        return InstalledPackageInfo.findByDescriptor(venv.sitePackages, descriptor).files
    }
}
