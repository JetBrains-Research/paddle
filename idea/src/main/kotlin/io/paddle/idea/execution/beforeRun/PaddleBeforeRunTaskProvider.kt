package io.paddle.idea.execution.beforeRun

import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemBeforeRunTask
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemBeforeRunTaskProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import icons.PythonIcons
import io.paddle.idea.PaddleManager
import javax.swing.Icon

class PaddleBeforeRunTaskProvider(project: Project) : ExternalSystemBeforeRunTaskProvider(PaddleManager.ID, project, ID), DumbAware {
    companion object {
        val ID = Key.create<ExternalSystemBeforeRunTask>("Paddle.BeforeRunTask")
    }

    override fun getIcon(): Icon {
        return PythonIcons.Python.Python
    }

    override fun getTaskIcon(task: ExternalSystemBeforeRunTask): Icon {
        return PythonIcons.Python.Python
    }

    override fun createTask(runConfiguration: RunConfiguration): ExternalSystemBeforeRunTask {
        return ExternalSystemBeforeRunTask(ID, PaddleManager.ID)
    }
}
