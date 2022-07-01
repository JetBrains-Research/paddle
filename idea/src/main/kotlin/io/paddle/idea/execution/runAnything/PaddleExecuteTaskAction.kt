package io.paddle.idea.execution.runAnything

import com.intellij.execution.Executor
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.ide.actions.runAnything.RunAnythingManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.externalSystem.action.ExternalSystemAction
import com.intellij.openapi.externalSystem.model.ExternalSystemDataKeys
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.externalSystem.model.execution.ExternalTaskExecutionInfo
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import io.paddle.idea.PaddleManager

class PaddleExecuteTaskAction : ExternalSystemAction() {
    override fun isVisible(e: AnActionEvent): Boolean {
        if (!super.isVisible(e)) return false
        val projectsView = e.getData(ExternalSystemDataKeys.VIEW)
        return projectsView == null || PaddleManager.ID == getSystemId(e)
    }

    override fun isEnabled(e: AnActionEvent): Boolean = true

    override fun update(e: AnActionEvent) {
        val p = e.presentation
        p.isVisible = isVisible(e)
        p.isEnabled = isEnabled(e)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val runAnythingManager = RunAnythingManager.getInstance(project)
        runAnythingManager.show(PaddleRunAnythingProvider.HELP_COMMAND + " ", false, e)
    }

    companion object {
        fun runPaddle(project: Project, executor: Executor?, workDirectory: String, fullCommandLine: String) {
            val taskExecutionInfo = buildTaskInfo(workDirectory, fullCommandLine, executor)
            ExternalSystemUtil.runTask(taskExecutionInfo.settings, taskExecutionInfo.executorId, project, PaddleManager.ID)
            val configuration = ExternalSystemUtil.createExternalSystemRunnerAndConfigurationSettings(
                taskExecutionInfo.settings, project, PaddleManager.ID
            ) ?: return
            val runManager = RunManager.getInstance(project)
            val existingConfiguration = runManager.findConfigurationByTypeAndName(configuration.type, configuration.name)
            if (existingConfiguration == null) {
                runManager.setTemporaryConfiguration(configuration)
            } else {
                runManager.selectedConfiguration = existingConfiguration
            }
        }

        private fun buildTaskInfo(
            projectPath: String,
            fullCommandLine: String,
            executor: Executor?
        ): ExternalTaskExecutionInfo {
            val settings = ExternalSystemTaskExecutionSettings()
            settings.externalProjectPath = projectPath
            settings.taskNames = listOf(fullCommandLine) // FIXME: implement parser?
            settings.externalSystemIdString = PaddleManager.ID.toString()
            return ExternalTaskExecutionInfo(settings, executor?.id ?: DefaultRunExecutor.EXECUTOR_ID)
        }
    }
}
