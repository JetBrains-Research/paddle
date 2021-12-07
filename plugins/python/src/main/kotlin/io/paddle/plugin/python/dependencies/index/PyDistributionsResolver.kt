package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.dependencies.index.utils.*

object PyDistributionsResolver {
    fun resolve(name: PyPackageName, version: PyPackageVersion, repositories: PyPackagesRepositories): PyPackageUrl {
        val primaryUrl = resolve(name, version, repositories.primarySource)
        if (primaryUrl != null)
            return primaryUrl
        for (repo in repositories.extraSources) {
            val extraUrl = resolve(name, version, repo)
            if (extraUrl != null) {
                return extraUrl
            }
        }
        error("Could not resolve $name:$version within specified set of repositories.")
    }

    fun resolve(name: PyPackageName, version: PyPackageVersion, repository: PyPackagesRepository): PyPackageUrl? {
        TODO()
    }
}
