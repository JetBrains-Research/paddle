package io.paddle.project

import io.paddle.execution.local.LocalCommandExecutor
import io.paddle.plugin.Plugin
import io.paddle.terminal.TerminalUI
import io.paddle.utils.config.Configuration
import io.paddle.utils.ext.Extendable
import java.io.File

class Project(val config: Configuration) {
    interface Extension<V: Any> {
        val key: Extendable.Key<V>

        fun create(project: Project): V
    }

    val tasks = Tasks()
    val extensions = Extendable()
    val executor = LocalCommandExecutor()

    fun register(plugin: Plugin) {
        for (extension in plugin.extensions(this)) {
            extensions.register(extension.key, extension.create(this))
        }

        for (task in plugin.tasks(this)) {
            tasks.register(task)
        }
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
