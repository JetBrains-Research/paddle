package io.paddle.idea.execution.beforeRun

import com.intellij.execution.BeforeRunTask
import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemBeforeRunTask
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.externalSystem.service.project.settings.BeforeRunTaskImporter
import com.intellij.openapi.project.Project
import com.intellij.util.ObjectUtils

class PaddleBeforeRunTaskImporter : BeforeRunTaskImporter {
    override fun canImport(typeName: String): Boolean = typeName == "paddleTask"

    override fun process(
        project: Project,
        modelsProvider: IdeModifiableModelsProvider,
        runConfiguration: RunConfiguration,
        beforeRunTasks: MutableList<BeforeRunTask<*>>,
        configurationData: MutableMap<String, Any>
    ): MutableList<BeforeRunTask<*>> {

        val taskProvider = BeforeRunTaskProvider.getProvider(project, PaddleBeforeRunTaskProvider.ID) ?: return beforeRunTasks
        val task = taskProvider.createTask(runConfiguration) ?: return beforeRunTasks
        task.taskExecutionSettings.apply {
            ObjectUtils.consumeIfCast(configurationData["taskName"], String::class.java) { taskNames = listOf(it) }
            ObjectUtils.consumeIfCast(configurationData["projectPath"], String::class.java) { externalProjectPath = it }
        }
        task.isEnabled = true

        if (!beforeRunTasks.taskExists(task)) {
            beforeRunTasks.add(task)
        }

        return beforeRunTasks
    }
}

internal fun List<BeforeRunTask<*>>.taskExists(task: ExternalSystemBeforeRunTask): Boolean {
    return filterIsInstance<ExternalSystemBeforeRunTask>()
        .any {
            it.taskExecutionSettings.taskNames == task.taskExecutionSettings.taskNames &&
                it.taskExecutionSettings.externalProjectPath == task.taskExecutionSettings.externalProjectPath
        }
}
