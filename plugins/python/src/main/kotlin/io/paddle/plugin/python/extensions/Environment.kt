package io.paddle.plugin.python.extensions

import io.paddle.execution.ExecutionResult
import io.paddle.plugin.python.dependencies.InstalledPackageInfoDir
import io.paddle.plugin.python.dependencies.VenvDir
import io.paddle.plugin.python.dependencies.index.distributions.WheelPyDistributionInfo
import io.paddle.plugin.python.dependencies.index.webIndexer
import io.paddle.plugin.python.dependencies.packages.CachedPyPackage
import io.paddle.plugin.python.dependencies.packages.PyPackage
import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.dependencies.resolvers.PipResolver
import io.paddle.plugin.python.utils.jsonParser
import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.PaddleProject
import io.paddle.project.extensions.routeAsString
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.Hashable
import io.paddle.utils.hash.hashable
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString


val PaddleProject.environment: Environment
    get() = checkNotNull(extensions.get(Environment.Extension.key)) { "Could not load extension Environment for project $routeAsString" }

class Environment(val project: PaddleProject, val venv: VenvDir) : Hashable {

    val localInterpreterPath: Path
        get() = venv.getInterpreterPath(project)

    val pythonPath: String
        get() {
            val paths =
                listOf(project.roots.sources.canonicalPath) + project.subprojects.map { it.environment.pythonPath }
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
            project.globalInterpreter.resolved.path.toString(),
            listOf("-m", "venv", venv.absolutePath),
            project.workDir,
            project.terminal
        ).then {
            // Need to create PyPackage.json for automatically installed setuptools package
            venv.sitePackages.listFiles()
                ?.firstOrNull { it.name.startsWith("setuptools") && it.name.endsWith(".dist-info") && it.isDirectory }
                ?.let {
                    val version = it.name.substringAfter("setuptools-").substringBefore(".dist-info")
                    val distributionUrl = runBlocking {
                        project.webIndexer.getDistributionUrl(
                            WheelPyDistributionInfo.fromString("setuptools-$version-py3-none-any.whl")!!,
                            PyPackageRepository.PYPI_REPOSITORY
                        ) ?: project.webIndexer.getDistributionUrl(
                            WheelPyDistributionInfo.fromString("setuptools-$version-py2.py3-none-any.whl")!!,
                            PyPackageRepository.PYPI_REPOSITORY
                        )
                    }
                        ?: error("Could not find setuptools==$version in PyPI repository. Please, consider re-creating your virtual environment using Paddle.")
                    val pkg = PyPackage("setuptools", version, PyPackageRepository.PYPI_REPOSITORY, distributionUrl)
                    val infoDir = InstalledPackageInfoDir(
                        it, InstalledPackageInfoDir.Companion.Type.DIST, "setuptools", version
                    )
                    infoDir.addFile(
                        CachedPyPackage.PYPACKAGE_CACHE_FILENAME,
                        jsonParser.encodeToString(PyPackage.serializer(), pkg)
                    )
                }
            project.executor.execute(
                localInterpreterPath.absolutePathString(),
                listOf("-m", "pip", "install", "pip-autoremove", PipResolver.PIP_RESOLVER_URL),
                project.workDir,
                project.terminal
            )
        }
    }

    fun runModule(module: String, arguments: List<String> = emptyList()): ExecutionResult {
        return project.executor.execute(
            localInterpreterPath.absolutePathString(),
            listOf("-m", module, *arguments.toTypedArray()),
            project.workDir,
            project.terminal,
            hashMapOf("PYTHONPATH" to project.environment.pythonPath)
        )
    }

    fun runScript(file: String, arguments: List<String> = emptyList()): ExecutionResult {
        return project.executor.execute(
            localInterpreterPath.absolutePathString(),
            listOf(file, *arguments.toTypedArray()),
            project.workDir,
            project.terminal,
            hashMapOf("PYTHONPATH" to project.environment.pythonPath)
        )
    }

    fun install(pkg: PyPackage) {
        // Exactly the same package has been already installed
        if (venv.hasInstalledPackage(pkg)) return

        // The package with the same name (but different version) had been installed previously and should be removed now
        venv.findPackageWithNameOrNull(pkg.name)?.let { uninstall(it) }

        val cachedPkg = project.globalCache.findOrInstallPackage(pkg)
        project.globalCache.createSymlinkToPackage(cachedPkg, venv)
    }

    fun uninstall(pkg: PyPackage) {
        project.executor.execute(
            command = venv.bin.resolve("pip-autoremove").canonicalPath,
            args = listOf(pkg.name, "-y"),
            workingDir = project.workDir,
            terminal = project.terminal
        ).expose(
            onSuccess = {
                project.terminal.info("Successfully removed the old version of package: ${pkg.name}==${pkg.version}")
            },
            onFail = {
                project.terminal.error("Failed to remove the old version of package: ${pkg.name}==${pkg.version}")
            }
        )
    }

    override fun hash(): String {
        return venv.hashable().hash()
    }
}
