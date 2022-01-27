package io.paddle.project

import io.paddle.execution.CommandExecutor
import io.paddle.execution.local.LocalCommandExecutor
import io.paddle.plugin.Plugin
import io.paddle.plugin.standard.extensions.Plugins
import io.paddle.terminal.*
import io.paddle.utils.StringHashable
import io.paddle.utils.config.Configuration
import io.paddle.utils.ext.Extendable
import java.io.File

class Project(val config: Configuration, val workDir: File = File("."), val output: TextOutput = TextOutput.Console) {
    interface Extension<V: Any> {
        val key: Extendable.Key<V>

        fun create(project: Project): V
    }

    val id: String = StringHashable(workDir.absolutePath).hash()
    val tasks = Tasks()
    val extensions = Extendable()
    var executor: CommandExecutor = LocalCommandExecutor(output)
    val terminal = Terminal(output)

    init {
        extensions.register(Plugins.Extension.key, Plugins.Extension.create(this))
    }

    fun register(plugin: Plugin) {
        for (extension in plugin.extensions(this)) {
            extensions.register(extension.key, extension.create(this))
        }

        for (task in plugin.tasks(this)) {
            tasks.register(task)
        }

        plugin.configure(this)
    }

    fun register(plugins: Iterable<Plugin>) {
        plugins.forEach { this.register(it) }
    }

    fun execute(id: String) {
        val task = tasks.get(id) ?: run {
            terminal.commands.stderr(CommandOutput.Command.Task(id, CommandOutput.Command.Task.Status.UNKNOWN))
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
