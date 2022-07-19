package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.dependencies.repositories.PyPackageRepository
import io.paddle.plugin.python.utils.takeIfAllAreEqual
import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.PaddleProject
import io.paddle.project.extensions.routeAsString
import io.paddle.utils.ext.Extendable
import java.io.File

val PaddleProject.publishEnvironment: PublishEnvironment
    get() = checkNotNull(extensions.get(PublishEnvironment.Extension.key)) { "Could not load extension PublishEnvironment for project $routeAsString" }

class PublishEnvironment(val twine: TwineEnvironment, val repo: PyPackageRepository?, val project: PaddleProject) {
    object Extension : PaddleProject.Extension<PublishEnvironment> {
        override val key: Extendable.Key<PublishEnvironment> = Extendable.Key()

        private fun getInstance(currentProject: PaddleProject, originalProject: PaddleProject): PublishEnvironment? {
            val repoName = currentProject.config.get<String>("tasks.publish.repo") ?: return null
            val repo = currentProject.repositories.resolved.findByName(repoName)
                ?: error(
                    "Could not find existing PyPI repository with name = $repoName for project ${currentProject.routeAsString}. " +
                        "Please, make sure that you have specified it in the <repositories> section of ${currentProject.buildFile.path} " +
                        "or in the <all.repositories> section of some parental project."
                )

            val skipExisting = currentProject.config.get<String>("tasks.publish.twine.skipExisting")?.toBoolean() ?: false
            val verbose = currentProject.config.get<String>("tasks.publish.twine.verbose")?.toBoolean() ?: false

            val targets = currentProject.config.get<List<String>>("tasks.publish.twine.targets")
                ?.map { originalProject.roots.dist.resolve(it).relativeTo(currentProject.workDir).path }
                ?: listOf(originalProject.roots.dist.absolutePath.trimEnd(File.separatorChar) + File.separator + "*")

            return PublishEnvironment(TwineEnvironment(skipExisting, verbose, targets), repo, currentProject)
        }

        override fun create(project: PaddleProject): PublishEnvironment {
            return getInstance(project, project)
                ?: project.parents.mapNotNull { getInstance(it, project) }.takeIfAllAreEqual()
                    ?.firstOrNull()?.also {
                        project.terminal.warn("Twine configuration for project ${project.routeAsString} was determined automatically: $it")
                    }
                ?: PublishEnvironment(TwineEnvironment(), null, project) // a lazy stub, it will fail if the user runs <publish> task later
        }
    }

    data class TwineEnvironment(val skipExisting: Boolean = false, val verbose: Boolean = false, val targets: List<String> = emptyList())
}
