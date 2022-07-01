package io.paddle.idea.execution.state

import com.intellij.execution.configurations.RunProfileState
import com.intellij.openapi.module.ModuleManager
import com.jetbrains.python.run.targetBasedConfiguration.PyRunTargetVariant
import com.jetbrains.python.sdk.basePath
import com.jetbrains.python.sdk.pythonSdk
import com.jetbrains.python.testing.PyPyTestExecutionEnvironment
import com.jetbrains.python.testing.PythonTestConfigurationType
import io.paddle.plugin.python.extensions.pytest
import io.paddle.plugin.python.tasks.test.PyTestTask

class PyPyTestExecutionEnvironmentProvider : PaddleTaskRunProfileStateProvider<PyTestTask> {
    override fun getState(task: PyTestTask, context: PaddleTaskRunProfileStateContext): RunProfileState? {
        val project = context.environment.project
        val module = ModuleManager.getInstance(context.environment.project).modules
            .find { it.basePath == context.moduleDir.absolutePath }
            ?: return null
        val factory = PythonTestConfigurationType.getInstance().pyTestFactory

        val pyTestConfiguration = factory.createTemplateConfiguration(project)
        pyTestConfiguration.apply {
            target.apply {
                target = task.project.pytest.targets.first().absolutePath // TODO: compound configurations for multiple targets
                targetType = PyRunTargetVariant.PATH
            }
            task.project.pytest.keywords?.let { keywords = it }
            additionalArguments = task.project.pytest.additionalArguments.joinToString(" ")
            sdkHome = module.pythonSdk?.homePath
        }

        updateBeforeRunTasks(task, context)

        return PyPyTestExecutionEnvironment(pyTestConfiguration, context.environment)
    }
}
