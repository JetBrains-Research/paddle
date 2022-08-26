package io.paddle.plugin.python.dependencies.repositories

import io.paddle.plugin.python.dependencies.authentication.authProvider
import io.paddle.plugin.python.dependencies.index.distributions.PyDistributionInfo
import io.paddle.plugin.python.extensions.Repositories
import io.paddle.plugin.python.extensions.pyLocations
import io.paddle.plugin.python.utils.PyPackageName
import io.paddle.plugin.python.utils.isValidUrl
import io.paddle.plugin.python.utils.parallelForEach
import io.paddle.project.PaddleProject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

class PyPackageRepositories(
    private val repositories: Set<PyPackageRepository>,
    val primarySource: PyPackageRepository,
    val project: PaddleProject,
    useCachedIndex: Boolean = true,
    downloadIndex: Boolean = false
) {
    companion object {
        fun resolve(repoDescriptors: List<Repositories.Descriptor>, project: PaddleProject): PyPackageRepositories {
            val repositories = hashSetOf(PyPackageRepository.PYPI_REPOSITORY)
            var primarySource = PyPackageRepository.PYPI_REPOSITORY

            for (descriptor in repoDescriptors) {
                require(descriptor.url.isValidUrl()) { "The provided url is invalid: ${descriptor.url}" }

                val repo = PyPackageRepository(descriptor)

                val default = descriptor.default ?: false
                val secondary = descriptor.secondary ?: false
                if (!secondary) {
                    primarySource = repo
                }
                if (default) {
                    repositories.remove(PyPackageRepository.PYPI_REPOSITORY)
                    primarySource = repo
                }

                repositories.add(repo)
            }

            return PyPackageRepositories(repositories, primarySource, project)
        }

        private fun updateIndex(repositories: Set<PyPackageRepository>, project: PaddleProject) = runBlocking {
            repositories.parallelForEach {
                try {
                    it.updateIndex(project)
                    it.saveCache(project)
                } catch (e: Throwable) {
                    project.terminal.warn(
                        "Failed to update index for PyPI repository: ${it.urlSimple}. " +
                                "Autocompletion for package names will not be available at the moment."
                    )
                }
            }
        }
    }

    init {
        if (useCachedIndex) {
            val cachedFiles = project.pyLocations.indexDir.toFile().listFiles() ?: emptyArray()
            val newRepositories = HashSet<PyPackageRepository>()
            for (repo in repositories) {
                cachedFiles.find { it.name == repo.cacheFileName }
                    ?.let { repo.loadCache(it) }
                    ?: newRepositories.add(repo)
            }
            updateIndex(newRepositories, project)
        }

        if (downloadIndex) {
            updateIndex(repositories, project)
        }
    }

    fun findAvailablePackagesByPrefix(prefix: String): Map<PyPackageName, PyPackageRepository> =
        HashMap<PyPackageName, PyPackageRepository>().apply {
            for ((repo, names) in repositories.associateWith { it.getPackagesNamesByPrefix(prefix).toList() }) {
                for (pkgName in names) {
                    if (!containsKey(pkgName) || repo == primarySource) {
                        put(pkgName, repo)
                    }
                }
            }
        }

    fun findAvailableDistributionsByPackageName(packageName: String): Map<PyDistributionInfo, PyPackageRepository> =
        runBlocking {
            val repoToDistributions = repositories
                .associateWith {
                    async {
                        it.findAvailableDistributionsByPackageName(packageName, project)
                    }
                }
                .run {
                    this.keys
                        .zip(this.values.awaitAll())
                        .toMap()
                }
            return@runBlocking HashMap<PyDistributionInfo, PyPackageRepository>().apply {
                for ((repo, distributions) in repoToDistributions) {
                    for (distribution in distributions) {
                        if (!containsKey(distribution) || repo == primarySource) {
                            put(distribution, repo)
                        }
                    }
                }
            }
        }

    val all: Set<PyPackageRepository>
        get() = repositories.toSet()

    val extraSources: Set<PyPackageRepository>
        get() = repositories.filter { it != primarySource }.toSet()

    val asPipArgs: List<String>
        get() = ArrayList<String>().apply {
            val primarySourceCredentials = project.authProvider
                .resolveCredentials(this@PyPackageRepositories.primarySource)
            add("--index-url")
            add(primarySourceCredentials.authenticate(primarySource.urlSimple))
            for (repo in this@PyPackageRepositories.repositories) {
                if (repo != this@PyPackageRepositories.primarySource) {
                    val credentials = project.authProvider.resolveCredentials(repo)
                    add("--extra-index-url")
                    add(credentials.authenticate(repo.urlSimple))
                }
            }
        }

    fun findByName(name: String): PyPackageRepository? {
        return repositories.find { it.name == name }
    }
}
