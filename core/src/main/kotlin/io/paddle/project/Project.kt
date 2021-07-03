package io.paddle.project

import io.paddle.terminal.TerminalUI
import io.paddle.utils.config.Configuration
import java.io.File

class Project(
    val config: Configuration,
    val roots: Roots,
    val tasks: Tasks,
    val requirements: Requirements,
    val locations: Locations,
    val environment: Environment,
    val subprojects: List<Project>
) {
    fun execute(id: String) {
        val task = tasks.get(id) ?: run {
            TerminalUI.echoln("> Task :$id: ${TerminalUI.colored("UNKNOWN", TerminalUI.Color.RED)}")
            return
        }
        task.run()
    }

    companion object {
        fun load(file: File): Project {
            val configuration = Configuration.from(file)
            val project = Project(
                roots = Roots.from(configuration),
                tasks = Tasks(),
                requirements = Requirements.from(configuration),
                locations = Locations.from(configuration),
                environment = Environment.from(configuration),
                subprojects = emptyList(),
                config = configuration
            )
            return project
        }
    }
}
