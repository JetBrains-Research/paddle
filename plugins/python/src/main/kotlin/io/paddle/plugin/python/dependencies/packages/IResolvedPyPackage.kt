package io.paddle.plugin.python.dependencies.packages

import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.utils.*

interface IResolvedPyPackage {
    val name: PyPackageName
    val version: PyPackageVersion
    val repo: PyPackageRepository
    val distributionUrl: PyPackageUrl
}
