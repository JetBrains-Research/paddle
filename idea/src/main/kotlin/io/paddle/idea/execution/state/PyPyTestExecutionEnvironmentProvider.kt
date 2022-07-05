package io.paddle.idea.execution.state

import com.intellij.execution.configurations.RunProfileState
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.module.ModuleManager
import com.jetbrains.python.run.targetBasedConfiguration.PyRunTargetVariant
import com.jetbrains.python.sdk.basePath
import com.jetbrains.python.sdk.pythonSdk
import com.jetbrains.python.testing.PyPyTestExecutionEnvironment
import com.jetbrains.python.testing.PythonTestConfigurationType
import io.paddle.plugin.python.tasks.test.PyTestTask

class PyPyTestExecutionEnvironmentProvider : PaddleTaskRunProfileStateProvider<PyTestTask> {
    override fun getState(task: PyTestTask, context: PaddleTaskRunProfileStateContext): RunProfileState? {
        val project = context.environment.project
        val module = ModuleManager.getInstance(context.environment.project).modules.find { it.basePath == context.moduleDir.absolutePath } ?: return null
        val factory = PythonTestConfigurationType.getInstance().pyTestFactory

        val pyTestConfiguration = factory.createTemplateConfiguration(project)
        pyTestConfiguration.apply {
            target.apply {
                // TODO: compound configurations for multiple targets
                // https://intellij-support.jetbrains.com/hc/en-us/community/posts/360003520439-Run-multiple-test-using-PyTest-and-module-names
                if (task.targets.isEmpty()) {
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Paddle")
                        .createNotification(
                            "At least one pytest target must be specified in <b>paddle.yaml</b>",
                            NotificationType.ERROR
                        ).notify(project)
                    return null
                } else {
                    target = task.targets.first().absolutePath
                }
                targetType = PyRunTargetVariant.PATH
            }
            task.keywords?.let { keywords = it }
            additionalArguments = task.additionalArgs.joinToString(" ")
            sdkHome = module.pythonSdk?.homePath
        }

        updateBeforeRunTasks(task, context)

        return PyPyTestExecutionEnvironment(pyTestConfiguration, context.environment)
    }
}
