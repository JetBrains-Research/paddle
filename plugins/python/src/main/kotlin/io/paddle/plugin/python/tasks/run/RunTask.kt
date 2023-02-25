package io.paddle.plugin.python.tasks.run

import io.paddle.plugin.python.extensions.environment
import io.paddle.plugin.python.extensions.pythonCliConfig
import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.PaddleProject
import io.paddle.tasks.Task
import io.paddle.utils.config.ConfigurationView
import io.paddle.utils.tasks.TaskDefaultGroups
import java.io.File

class RunTask(val name: String, val entrypoint: String, val arguments: List<String>, project: PaddleProject) : Task(project) {
    val isModuleMode: Boolean
        get() = !entrypoint.endsWith(".py")


    companion object {
        fun from(project: PaddleProject): List<RunTask> {
            val configurations = project.config.get<List<Map<String, Any>>?>("tasks.run") ?: return emptyList()
            val tasks = ArrayList<RunTask>()
            val cliConfig = ConfigurationView("run", project.pythonCliConfig)
            for (configuration in configurations) {
                val args: List<String> = cliConfig.get<String>("extraArgs")?.split(" ") ?:
                    (configuration.getOrDefault("args", emptyList<String>()) as List<String>)

                val entrypointPath = project.roots.sources.resolve(configuration["entrypoint"] as String).relativeTo(project.workDir).path
                val entrypoint =
                    if (entrypointPath.endsWith(".py"))
                        entrypointPath
                    else
                        entrypointPath.replace(File.separatorChar, '.')

                tasks.add(
                    RunTask(
                        configuration["id"] as String,
                        entrypoint,
                        args,
                        project
                    )
                )
            }
            return tasks
        }
    }

    override val id: String = name

    override val group: String = TaskDefaultGroups.RUN

    override val dependencies: List<Task>
        get() = listOf(project.tasks.getOrFail("install"))

    override fun initialize() {}

    override fun act() {
        when {
            isModuleMode -> project.environment.runModule(entrypoint, arguments)
            else -> project.environment.runScript(entrypoint, arguments)
        }.orElse { throw ActException("Script has returned non-zero exit code: $it") }
    }
}
