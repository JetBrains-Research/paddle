package io.paddle.idea.execution.state

import com.intellij.execution.configurations.RunProfileState
import com.intellij.openapi.module.ModuleManager
import com.jetbrains.python.run.*
import com.jetbrains.python.sdk.basePath
import com.jetbrains.python.sdk.pythonSdk
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
            scriptName = if (task.isModuleMode) task.entrypoint else context.moduleDir.toPath().resolve(task.entrypoint).absolutePathString()
            scriptParameters = task.arguments.joinToString(" ")
            sdkHome = module.pythonSdk?.homePath
            isModuleMode = task.isModuleMode
            workingDirectory = context.rootDir.absolutePath
        }

        updateBeforeRunTasks(task, context)

        return PythonScriptCommandLineState(pythonRunConfiguration, context.environment)
    }
}
