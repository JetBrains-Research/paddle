package io.paddle.plugin.python.dependencies.packages

import io.paddle.plugin.python.dependencies.index.PyPackagesRepository
import io.paddle.plugin.python.utils.*

interface ResolvedPyPackage {
    val name: PyPackageName
    val version: PyPackageVersion
    val repo: PyPackagesRepository
    val distributionUrl: PyPackageUrl
}
