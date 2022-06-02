package io.paddle.idea.execution.providers

import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.RunManagerEx
import com.intellij.execution.configurations.RunProfileState
import com.intellij.openapi.module.ModuleManager
import com.jetbrains.python.run.*
import com.jetbrains.python.sdk.basePath
import com.jetbrains.python.sdk.pythonSdk
import io.paddle.idea.execution.beforeRun.PaddleBeforeRunTaskProvider
import io.paddle.idea.execution.beforeRun.taskExists
import io.paddle.plugin.python.tasks.run.RunTask
import kotlin.io.path.absolutePathString

class PythonScriptCommandLineStateProvider : PaddleTaskRunProfileStateProvider<RunTask> {
    override fun getState(task: RunTask, context: PaddleTaskRunProfileStateContext): RunProfileState? {
        val factory = PythonConfigurationType.getInstance().factory
        val module = ModuleManager.getInstance(context.environment.project).modules
            .find { it.basePath == context.moduleDir.absolutePath }
            ?: return null

        val pythonRunConfiguration = factory.createTemplateConfiguration(context.environment.project) as PythonRunConfiguration
        pythonRunConfiguration.apply {
            scriptName = context.moduleDir.toPath().resolve(task.entrypoint).absolutePathString()
            scriptParameters = task.arguments.joinToString(" ")
            sdkHome = module.pythonSdk?.homePath
            isModuleMode = task.isModuleMode
        }

        updateBeforeRunTasks(task, context)

        return PythonScriptCommandLineState(pythonRunConfiguration, context.environment)
    }

    private fun updateBeforeRunTasks(task: RunTask, context: PaddleTaskRunProfileStateContext) {
        val project = context.environment.project
        val beforeRunTaskProvider = BeforeRunTaskProvider.getProvider(project, PaddleBeforeRunTaskProvider.ID) as? PaddleBeforeRunTaskProvider
        val runManager = RunManagerEx.getInstanceEx(project)

        val newBeforeRunTasks = task.dependencies.map { dependsOnTask ->
            beforeRunTaskProvider?.createTask(context.originalRunConfiguration)
                ?.apply {
                    taskExecutionSettings.externalProjectPath = dependsOnTask.project.workDir.absolutePath
                    taskExecutionSettings.taskNames = listOf(dependsOnTask.id)
                    isEnabled = true
                } ?: return
        }

        val oldBeforeRunTasks = runManager.getBeforeRunTasks(context.originalRunConfiguration)
        val beforeRunTasks = oldBeforeRunTasks + newBeforeRunTasks.filter { !oldBeforeRunTasks.taskExists(it) }

        runManager.setBeforeRunTasks(context.originalRunConfiguration, beforeRunTasks)
    }
}
