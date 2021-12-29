package io.paddle.plugin.python.dependencies.index

import io.paddle.plugin.python.extensions.Requirements
import io.paddle.plugin.python.utils.*
import io.paddle.project.Project
import kotlinx.coroutines.runBlocking

/**
 * Resolved version of `Requirements.Descriptor`
 */
class PyPackage(
    val descriptor: Requirements.Descriptor,
    val repo: PyPackagesRepository,
    val distributionUrl: PyPackageUrl,
) {
    val name = descriptor.name
    val version = descriptor.version

    companion object {
        fun resolve(descriptor: Requirements.Descriptor, project: Project): PyPackage = runBlocking {
            val (url, repo) = PyDistributionsResolver.resolve(descriptor, project)
            return@runBlocking PyPackage(descriptor, repo, url)
        }

        fun resolve(name: PyPackageName, version: PyPackageVersion, project: Project): PyPackage = runBlocking {
            val descriptor = Requirements.Descriptor(name, version, repo = null)
            val (url, repo) = PyDistributionsResolver.resolve(descriptor, project)
            return@runBlocking PyPackage(descriptor, repo, url)
        }
    }
}
