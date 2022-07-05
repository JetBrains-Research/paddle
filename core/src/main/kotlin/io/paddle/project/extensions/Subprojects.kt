package io.paddle.project.extensions

import io.paddle.project.PaddleProject
import io.paddle.project.PaddleProjectIndex
import io.paddle.tasks.Task
import io.paddle.utils.config.ConfigurationChain
import io.paddle.utils.config.ConfigurationYAML

val PaddleProject.route: List<String>
    get() = ((this.parents.maxByOrNull { it.route.size }?.route ?: emptyList()) + this.descriptor.name)

class Subprojects(private val subprojects: List<PaddleProject>) : Iterable<PaddleProject> {
    companion object {
        internal fun create(project: PaddleProject, index: PaddleProjectIndex): Subprojects {
            val names = project.config.get<List<String>>("subprojects") ?: return Subprojects(emptyList())
            val subprojects = ArrayList<PaddleProject>()

            // Load subprojects model using index
            for (name in names) {
                index.getProjectByName(name)?.let {
                    subprojects.add(it)
                    it.parents.add(project)
                } ?: throw SubprojectsInitializationException("Subproject :$name was not found for project :${project.descriptor.name}")
            }

            // Load additional configurations for subprojects from section [all] in the parental project
            project.config.get<Map<String, Any>>("all")?.also {
                val allConfig = ConfigurationYAML(it)

                require(allConfig.get<Any?>("descriptor") == null) { "You can not specify the same [descriptor] for ALL projects at a time." }
                require(allConfig.get<Any?>("subprojects") == null) { "You can not specify the same [subprojects] for ALL projects at a time." }

                (subprojects + project).forEach { subproject ->
                    subproject.config = ConfigurationChain(subproject.config, allConfig)
                }
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
