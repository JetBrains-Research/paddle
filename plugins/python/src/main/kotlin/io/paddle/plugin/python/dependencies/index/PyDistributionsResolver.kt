package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.dependencies.index.PyPackagesRepositories
import io.paddle.plugin.python.dependencies.index.utils.*
import io.paddle.plugin.python.extensions.Requirements

object PyDistributionsResolver {
    fun resolve(name: PyPackageName, version: PyPackageVersion, repositories: PyPackagesRepositories): PyPackageUrl {
        TODO()
    }

    fun resolve(name: PyPackageName, version: PyPackageVersion, repository: PyPackagesRepository): PyPackageUrl {
        TODO()
    }
}
