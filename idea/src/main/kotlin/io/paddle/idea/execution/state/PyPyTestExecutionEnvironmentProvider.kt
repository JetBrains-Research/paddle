package io.paddle.idea.execution.state

import com.intellij.execution.configurations.RunProfileState
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.jetbrains.python.run.targetBasedConfiguration.PyRunTargetVariant
import com.jetbrains.python.sdk.basePath
import com.jetbrains.python.sdk.pythonSdk
import com.jetbrains.python.testing.*
import io.paddle.plugin.python.dependencies.pytest.PyTestTarget
import io.paddle.plugin.python.tasks.test.PyTestTask

class PyPyTestExecutionEnvironmentProvider : PaddleTaskRunProfileStateProvider<PyTestTask> {
    override fun getState(task: PyTestTask, context: PaddleTaskRunProfileStateContext): RunProfileState? {
        val project = context.environment.project
        val module = ModuleManager.getInstance(context.environment.project).modules.find { it.basePath == context.moduleDir.absolutePath } ?: return null
        val factory = PythonTestConfigurationType.getInstance().pyTestFactory

        val pyTestConfiguration = createPyTestRunConfiguration(factory, project, task.targets.first(), task, module)

        updateBeforeRunTasks(task, context)

        return PyPyTestExecutionEnvironment(pyTestConfiguration, context.environment)
    }

    companion object {
        internal fun createPyTestRunConfiguration(
            factory: PyTestFactory,
            project: Project,
            pyTestTarget: PyTestTarget,
            task: PyTestTask,
            module: Module
        ): PyTestConfiguration {
            val pyTestConfiguration = factory.createTemplateConfiguration(project)
            pyTestConfiguration.apply {
                target.apply {
                    target = pyTestTarget.pycharmArgument
                    targetType = when (pyTestTarget.type) {
                        PyTestTarget.Type.DIRECTORY, PyTestTarget.Type.FILE -> PyRunTargetVariant.PATH
                        PyTestTarget.Type.NODE_ID -> PyRunTargetVariant.PYTHON
                    }
                }
                task.keywords?.let { keywords = it }
                additionalArguments = task.additionalArgs.joinToString(" ")
                sdkHome = module.pythonSdk?.homePath
                workingDirectory = task.project.workDir.canonicalPath
            }
            return pyTestConfiguration
        }
    }
}
