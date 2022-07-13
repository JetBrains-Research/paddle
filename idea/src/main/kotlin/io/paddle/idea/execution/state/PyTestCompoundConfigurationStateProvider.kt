package io.paddle.idea.execution.state

import com.intellij.execution.ExecutionTargetManager
import com.intellij.execution.RunManager
import com.intellij.execution.compound.CompoundRunConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ExecutionUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.ModuleManager
import com.jetbrains.python.sdk.basePath
import com.jetbrains.python.testing.PythonTestConfigurationType
import io.paddle.idea.execution.state.PyPyTestExecutionEnvironmentProvider.Companion.createPyTestRunConfiguration
import io.paddle.plugin.python.tasks.test.PyTestTask

class PyTestCompoundConfigurationStateProvider : PaddleTaskRunProfileStateProvider<PyTestTask> {
    override fun getState(task: PyTestTask, context: PaddleTaskRunProfileStateContext): RunProfileState? {
        val project = context.environment.project
        val module = ModuleManager.getInstance(context.environment.project).modules.find { it.basePath == context.moduleDir.absolutePath } ?: return null
        val pyTestFactory = PythonTestConfigurationType.getInstance().pyTestFactory

        val compoundConfiguration = CompoundRunConfiguration(context.originalRunConfiguration.name, project)
        val configurations = task.targets.map { pyTestTarget ->
            createPyTestRunConfiguration(pyTestFactory, project, pyTestTarget, task, module)
        }
        compoundConfiguration.setConfigurationsWithTargets(configurations.associateWith { ExecutionTargetManager.getActiveTarget(project) })

        updateBeforeRunTasks(task, context)

        return RunProfileState { _, _ ->
            ApplicationManager.getApplication().invokeLater {
                val groupId = ExecutionEnvironment.getNextUnusedExecutionId()
                for ((configuration, target) in compoundConfiguration.getConfigurationsWithEffectiveRunTargets()) {
                    val settings = RunManager.getInstance(project).createConfiguration(configuration, pyTestFactory)
                    ExecutionUtil.doRunConfiguration(settings, context.executor, target, groupId, null)
                }
            }
            null
        }
    }
}
