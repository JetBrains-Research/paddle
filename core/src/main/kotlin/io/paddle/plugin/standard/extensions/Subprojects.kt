package io.paddle.plugin.standard.extensions

import io.paddle.project.Project
import io.paddle.tasks.Task
import io.paddle.utils.ext.Extendable

val Project.route: String
    get() = (this.parent?.route ?: "") + ":${this.descriptor.name}"

val Project.subprojects: Subprojects
    get() = this.extensions.get(Subprojects.Extension.key)!!

class Subprojects(private val subprojects: List<Project>) : Iterable<Project> {
    object Extension : Project.Extension<Subprojects> {
        override val key: Extendable.Key<Subprojects> = Extendable.Key()

        override fun create(project: Project): Subprojects {
            val names = project.config.get<List<String>>("subprojects") ?: emptyList()
            val subprojects = ArrayList<Project>()

            for (name in names) {
                val workDir = project.workDir.resolve(name)
                if (!workDir.exists() && !workDir.isDirectory) {
                    throw SubprojectsInitializationException("Subproject $name was not found at ${project.workDir.path}")
                }
                subprojects.add(
                    Project.load(
                        file = workDir.resolve("paddle.yaml"),
                        workDir = workDir,
                        parent = project
                    ).also {
                        it.register(it.plugins.enabled)
                    }
                )
            }

            return Subprojects(subprojects)
        }
    }

    fun getByName(name: String): Project? {
        return subprojects.find { it.descriptor.name == name }
    }

    fun getAllTasksById(id: String): List<Task> {
        return subprojects.mapNotNull { it.tasks.get(id) }
    }

    class SubprojectsInitializationException(reason: String) : Exception(reason)

    override fun iterator(): Iterator<Project> = subprojects.iterator()
}
