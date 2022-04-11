package io.paddle.plugin.pyinjector.dependencies.repositories

import io.paddle.plugin.pyinjector.dependencies.PyModule
import io.paddle.plugin.pyinjector.dependencies.PyModuleName
import io.paddle.plugin.pyinjector.extensions.PyPluginsRepositories
import io.paddle.plugin.pyinjector.utils.Metafile
import io.paddle.utils.exists
import java.nio.file.Path

class PyModuleRepositories(private val storage: Map<PyModuleRepoName, PyModuleRepository>) {
    companion object {
        private const val metafileName = "paddle-plugins.yaml"

        fun resolve(workingDir: Path, descriptions: List<PyPluginsRepositories.ModuleRepoDescriptor>): PyModuleRepositories {
            val repos: MutableMap<PyModuleRepoName, PyModuleRepository> = hashMapOf()
            descriptions.forEach {
                repos[it.name] = resolve(workingDir, it)
            }
            return PyModuleRepositories(repos)
        }

        private fun resolve(workingDir: Path, description: PyPluginsRepositories.ModuleRepoDescriptor): PyModuleRepository {
            val repoPath = workingDir.resolve(description.directory)
            require(repoPath.exists())
            val metafilePath = repoPath.resolve(metafileName)
            require(metafilePath.exists())
            val modulesDescriptions: List<PyModule.Description> = Metafile.parse(metafilePath.toFile())

            return PyModuleRepository(description.name, repoPath.toAbsolutePath(), retrieveModules(repoPath, modulesDescriptions))
        }

        private fun retrieveModules(pathToRepo: Path, descriptions: List<PyModule.Description>): Map<PyModuleName, PyModule> {
            val (withPaths, withNames) = descriptions.partition {
                pathToRepo.resolve(it.filenameOrPath).exists()
            }
            val storage = withPaths.associateByTo(
                hashMapOf(), { it.name },
                { PyModule(it.name, pathToRepo.resolve(it.filenameOrPath).toAbsolutePath()) }
            )
            val withFilenamesToResolve = withNames.associateByTo(hashMapOf(), PyModule.Description::filenameOrPath)
            pathToRepo.toFile().walkTopDown().forEach {
                if (it.isFile) {
                    (withFilenamesToResolve.remove(it.nameWithoutExtension) ?: withFilenamesToResolve.remove(it.name))?.apply {
                        storage[name] = PyModule(name, it.toPath().toAbsolutePath())
                    }
                }
            }
            requireAllModulesResolved(withFilenamesToResolve.values)
            return storage
        }

        private fun requireAllModulesResolved(descriptors: Collection<PyModule.Description>) {
            descriptors.firstOrNull()?.also {
                throw IllegalArgumentException(
                    "Cannot find module `${it.name}` with file `${it.filenameOrPath}`. " +
                        "Ensure that file exists or filename contains only name or absolute or relative path to repository directory"
                )
            }
        }
    }

    operator fun get(repoName: PyModuleRepoName): PyModuleRepository? = storage[repoName]

    operator fun get(repoName: PyModuleRepoName, moduleName: PyModuleName): PyModule? = storage[repoName]?.get(moduleName)

    fun findAvailablePluginsBy(prefix: String): Map<PyModuleName, PyModuleRepository> {
        TODO("implement")
    }
}
