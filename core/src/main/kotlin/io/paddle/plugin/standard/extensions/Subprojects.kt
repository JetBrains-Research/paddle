package io.paddle.plugin.standard.extensions

import io.paddle.project.PaddleProject
import io.paddle.project.provider
import io.paddle.tasks.Task
import io.paddle.utils.ext.Extendable

val PaddleProject.route: List<String>
    get() = ((this.parents.maxByOrNull { it.route.size }?.route ?: emptyList()) + this.descriptor.name)

val PaddleProject.subprojects: Subprojects
    get() = this.extensions.get(Subprojects.Extension.key)!!

class Subprojects(private val subprojects: List<PaddleProject>) : Iterable<PaddleProject> {
    object Extension : PaddleProject.Extension<Subprojects> {
        override val key: Extendable.Key<Subprojects> = Extendable.Key()

        override fun create(project: PaddleProject): Subprojects {
            val names = project.config.get<List<String>>("subprojects") ?: return Subprojects(emptyList())
            val subprojects = ArrayList<PaddleProject>()

            for (name in names) {
                project.provider.findBy(name)?.let {
                    subprojects.add(it)
                    it.parents.add(project)
                } ?: throw SubprojectsInitializationException("Subproject :$name was not found for project :${project.descriptor.name}")
            }

            return Subprojects(subprojects)
        }
    }

    fun getByName(name: String): PaddleProject? {
        return subprojects.find { it.descriptor.name == name }
    }

    fun getAllTasksById(id: String): List<Task> {
        return subprojects.mapNotNull { it.tasks.get(id) }
    }

    class SubprojectsInitializationException(reason: String) : Exception(reason)

    override fun iterator(): Iterator<PaddleProject> = subprojects.iterator()
}
