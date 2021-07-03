package io.paddle.project

import io.paddle.project.config.Configuration
import io.paddle.tasks.TasksRegistrar
import java.io.File

class Project(
    val roots: Roots,
    val tasks: TasksRegistrar,
    val requirements: Requirements,
    val locations: Locations,
    val environment: Environment,
    val subprojects: List<Project>
) {
    fun execute(id: String) {
        val task = tasks.get(id)
        task?.run()
    }

    companion object {
        fun load(file: File): Project {
            val configuration = Configuration.from(file)
            val project = Project(
                roots = Roots.from(configuration),
                tasks = TasksRegistrar(),
                requirements = Requirements.from(configuration),
                locations = Locations.from(configuration),
                environment = Environment.from(configuration),
                subprojects = emptyList()
            )
            project.tasks.default(project, configuration)
            return project
        }
    }
}
