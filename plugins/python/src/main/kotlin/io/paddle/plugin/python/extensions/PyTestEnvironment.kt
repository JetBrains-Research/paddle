package io.paddle.plugin.python.extensions

import io.paddle.plugin.python.utils.PyPackageVersion
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.*
import java.io.File
import java.nio.file.Paths

val PaddleProject.pytest: PyTestEnvironment
    get() = extensions.get(PyTestEnvironment.Extension.key)!!

class PyTestEnvironment(
    val project: PaddleProject,
    val version: PyPackageVersion,
    val targets: List<File>,
    val keywords: String?,
    val additionalArguments: List<String>
) : Hashable {
    object Extension : PaddleProject.Extension<PyTestEnvironment> {
        override val key: Extendable.Key<PyTestEnvironment> = Extendable.Key()

        override fun create(project: PaddleProject): PyTestEnvironment {
            val version = project.config.get<String>("tasks.tests.pytest.version") ?: "7.1.2"

            val relativeTargets = project.config.get<List<String>>("tasks.tests.pytest.targets") ?: emptyList()
            val targets = ArrayList<File>()
            for (relativeTarget in relativeTargets) {
                if (Paths.get(relativeTarget).isAbsolute) {
                    targets.add(File(relativeTarget))
                }
                val absoluteTarget = project.workDir.resolve(relativeTarget).takeIf { it.exists() }
                    ?: throw Task.ActException("Pytest target $relativeTarget was not found at ${project.workDir.absolutePath}")
                targets.add(absoluteTarget)
            }

            val keywords = project.config.get<String>("tasks.tests.pytest.keywords")
                ?.takeUnless { it.isBlank() }
            val additionalArgs = project.config.get<String>("tasks.tests.pytest.parameters")
                ?.takeUnless { it.isBlank() }
                ?.split(" ")
                ?: emptyList()

            return PyTestEnvironment(project, version, targets, keywords, additionalArgs)
        }
    }

    override fun hash(): String {
        return (targets.map { FileHashable(it) } +
            additionalArguments.map { it.hashable() } +
            (keywords?.hashable() ?: EmptyHashable())
            ).hashable().hash()
    }
}
