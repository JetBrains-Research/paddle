package io.paddle.project

import io.paddle.execution.CommandExecutor
import io.paddle.execution.local.LocalCommandExecutor
import io.paddle.plugin.Plugin
import io.paddle.plugin.standard.extensions.Plugins
import io.paddle.plugin.standard.extensions.subprojects
import io.paddle.schema.extensions.BaseJsonSchemaExtension
import io.paddle.schema.extensions.JsonSchema
import io.paddle.terminal.*
import io.paddle.utils.config.Configuration
import io.paddle.utils.ext.Extendable
import io.paddle.utils.hash.StringHashable
import io.paddle.utils.yaml.YAML
import java.io.File

class PaddleProject internal constructor(val config: Configuration, val workDir: File, val rootDir: File, output: TextOutput = TextOutput.Console) {
    interface Extension<V : Any> {
        val key: Extendable.Key<V>

        fun create(project: PaddleProject): V
    }

    val id: String = "project_" + StringHashable(workDir.canonicalPath).hash()
    val tasks = Tasks()
    val extensions = Extendable()
    val parents = ArrayList<PaddleProject>()

    var output: TextOutput = output
        set(value) {
            field = value
            executor = LocalCommandExecutor(value)
            terminal = Terminal(value)
            subprojects.forEach { it.output = value }
        }
    var executor: CommandExecutor = LocalCommandExecutor(output)
    var terminal = Terminal(output)

    val buildFile: File = workDir.resolve("paddle.yaml")
    val yaml: MutableMap<String, Any> = buildFile.readText().let { YAML.parse(it) }

    init {
        extensions.register(Plugins.Extension.key, Plugins.Extension.create(this))
        extensions.register(JsonSchema.Extension.key, JsonSchema.Extension.create(this))
    }

    fun register(plugin: Plugin) {
        for (extension in plugin.extensions(this)) {
            val extensionToStorage = extension.create(this)
            // zhvkgj: implementation via marker annotation checks can be better than this one
            if (extensionToStorage is BaseJsonSchemaExtension) {
                extensions.get(JsonSchema.Extension.key)?.extensions?.add(extensionToStorage)
            } else {
                extensions.register(extension.key, extensionToStorage)
            }
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

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PaddleProject
        if (id != other.id) return false
        return true
    }

    class ProjectInitializationException(reason: String) : Exception(reason)
}

