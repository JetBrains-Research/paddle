package io.paddle.plugin.python.dependencies

import io.paddle.execution.ExecutionResult
import io.paddle.plugin.python.dependencies.authentication.authProvider
import io.paddle.plugin.python.dependencies.packages.CachedPyPackage.Companion.PYPACKAGE_CACHE_FILENAME
import io.paddle.plugin.python.dependencies.packages.IResolvedPyPackage
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.dependencies.resolvers.PipResolver
import io.paddle.plugin.python.extensions.*
import io.paddle.plugin.python.utils.*
import io.paddle.plugin.python.utils.jsonParser
import io.paddle.plugin.standard.extensions.locations
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.terminal.Terminal
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * A service for managing Paddle's internal virtual environment, where all the packages are installed for the first time.
 * Then, Paddle moves them to the corresponding ~/.paddle/cache/repo/package/version folder.
 */
class TempVenvManager private constructor(val venv: VenvDir, val project: PaddleProject) {
    companion object {
        @Volatile
        private var instance: TempVenvManager? = null

        fun getInstance(project: PaddleProject): TempVenvManager =
            instance ?: synchronized(this) {
                instance ?: getTempVenvManager(project).also { instance = it }
            }

        private fun getTempVenvManager(project: PaddleProject): TempVenvManager {
            val venv = VenvDir(project.pyLocations.venvsDir.resolve(project.id).toFile())
            createTempVenv(project, venv).orElse {
                throw Task.ActException("Failed to create Paddle's internal virtualenv. Check your python installation.")
            }
            return TempVenvManager(venv, project)
        }

        private fun createTempVenv(
            project: PaddleProject,
            venv: VenvDir,
            options: List<String> = emptyList(),
            verbose: Boolean = true
        ): ExecutionResult {
            return project.executor.execute(
                command = project.environment.localInterpreterPath.absolutePathString(),
                args = listOf("-m", "venv") + options + project.pyLocations.venvsDir.resolve(project.id).toString(),
                workingDir = project.locations.paddleHome.toFile(),
                terminal = Terminal.MOCK,
                verbose = verbose
            ).then {
                project.executor.execute(
                    command = venv.getInterpreterPath(project).absolutePathString(),
                    args = listOf("-m", "pip", "install", PipResolver.PIP_RESOLVER_URL),
                    workingDir = project.workDir,
                    terminal = Terminal.MOCK
                )
            }.then {
                project.executor.execute(
                    command = venv.getInterpreterPath(project).absolutePathString(),
                    args = listOf("-m", "pip", "install", "--upgrade", "pip"),
                    workingDir = project.workDir,
                    terminal = Terminal.MOCK
                )
            }
        }
    }

    private val interpreterPath: Path
        get() = venv.getInterpreterPath(project)

    fun install(pkg: PyPackage): ExecutionResult {
        // Specifying index/extra-index urls is redundant since distributionUrl already contains it as a part of URI
        val credentials = project.authProvider.resolveCredentials(pkg.repo)
        return project.executor.execute(
            command = interpreterPath.absolutePathString(),
            args = PipArgs.build("install") {
                packages = listOf(credentials.authenticate(pkg.distributionUrl))
                noCacheDir = project.pythonRegistry.noCacheDir
                noDeps = true
            }.args,
            workingDir = project.locations.paddleHome.toFile(),
            terminal = project.terminal
        ).also {
            val infoDir = InstalledPackageInfoDir.findByNameAndVersion(venv.sitePackages, pkg.name, pkg.version)
            infoDir.addFile(PYPACKAGE_CACHE_FILENAME, jsonParser.encodeToString(PyPackage.serializer(), pkg))
        }
    }

    fun uninstall(pkg: IResolvedPyPackage): ExecutionResult {
        return project.executor.execute(
            command = interpreterPath.absolutePathString(),
            args = listOf("-m", "pip", "uninstall", "-y", pkg.name),
            workingDir = project.locations.paddleHome.toFile(),
            terminal = Terminal.MOCK
        )
    }

    fun getFilesRelatedToPackage(pkg: IResolvedPyPackage): List<File> {
        return InstalledPackageInfoDir.findByNameAndVersion(venv.sitePackages, pkg.name, pkg.version).files
    }
}
