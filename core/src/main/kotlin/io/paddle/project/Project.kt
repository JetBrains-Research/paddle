package io.paddle.project

import io.paddle.plugin.Plugin
import io.paddle.terminal.TerminalUI
import io.paddle.utils.config.Configuration
import java.io.File

class Project(val config: Configuration) {
    val roots = Roots.from(config)
    val tasks = Tasks()
    val requirements = Requirements.from(config)
    val environment = Environment.from(config)

    fun register(plugin: Plugin) {
        tasks.register(*plugin.tasks(this).toTypedArray())
    }

    fun execute(id: String) {
        val task = tasks.get(id) ?: run {
            TerminalUI.echoln("> Task :$id: ${TerminalUI.colored("UNKNOWN", TerminalUI.Color.RED)}")
            return
        }
        task.run()
    }

    companion object {
        fun load(file: File): Project {
            return Project(config = Configuration.from(file))
        }
    }
}
