package io.paddle.idea.execution.state

import com.intellij.execution.configurations.RunProfileState
import com.intellij.openapi.module.ModuleManager
import com.jetbrains.python.run.*
import com.jetbrains.python.sdk.basePath
import com.jetbrains.python.sdk.pythonSdk
import io.paddle.Paddle
import io.paddle.idea.execution.PaddleRunConfiguration
import io.paddle.plugin.python.extensions.environment
import io.paddle.plugin.python.tasks.run.RunTask
import kotlin.io.path.absolutePathString

class PythonScriptCommandLineStateProvider : PaddleTaskRunProfileStateProvider<RunTask> {
    override fun getState(task: RunTask, context: PaddleTaskRunProfileStateContext): RunProfileState? {
        val factory = PythonConfigurationType.getInstance().factory
        val module = ModuleManager.getInstance(context.environment.project).modules
            .find { it.basePath == context.moduleDir.absolutePath }
            ?: return null

        val additionalArgsLine = (context.originalRunConfiguration as PaddleRunConfiguration).commandLine.tasksAndArguments.toList()
        val additionalArgs = Paddle.parseCliOptions(additionalArgsLine)["extraArgs"]?.trim('"', '\'') ?: ""

        val pythonRunConfiguration = factory.createTemplateConfiguration(context.environment.project) as PythonRunConfiguration
        pythonRunConfiguration.apply {
            scriptName = if (task.isModuleMode) task.entrypoint else context.moduleDir.toPath().resolve(task.entrypoint).absolutePathString()
            scriptParameters = (task.arguments).joinToString(" ") + " $additionalArgs"
            sdkHome = module.pythonSdk?.homePath
            isModuleMode = task.isModuleMode
            workingDirectory = context.moduleDir.absolutePath
            setEnvs(mapOf("PYTHONPATH" to task.project.environment.pythonPath))
            setAddContentRoots(false)
            setAddSourceRoots(false)
        }

        updateBeforeRunTasks(task, context)

        return PythonScriptCommandLineState(pythonRunConfiguration, context.environment)
    }
}
