package io.paddle.project

import io.paddle.execution.CommandExecutor
import io.paddle.execution.local.LocalCommandExecutor
import io.paddle.plugin.Plugin
import io.paddle.terminal.TerminalUI
import io.paddle.terminal.CommandOutput
import io.paddle.utils.config.Configuration
import io.paddle.utils.ext.Extendable
import java.io.File

class Project(val config: Configuration, val workDir: File = File("."), val output: CommandOutput = CommandOutput.Console) {
    interface Extension<V: Any> {
        val key: Extendable.Key<V>

        fun create(project: Project): V
    }

    val tasks = Tasks()
    val extensions = Extendable()
    var executor: CommandExecutor = LocalCommandExecutor(output)
    val terminal = TerminalUI(output)

    fun register(plugin: Plugin) {
        for (extension in plugin.extensions(this)) {
            extensions.register(extension.key, extension.create(this))
        }

        for (task in plugin.tasks(this)) {
            tasks.register(task)
        }

        plugin.configure(this)
    }

    fun execute(id: String) {
        val task = tasks.get(id) ?: run {
            terminal.stderr("> Task :$id: ${terminal.colored("UNKNOWN", TerminalUI.Color.RED)}")
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
