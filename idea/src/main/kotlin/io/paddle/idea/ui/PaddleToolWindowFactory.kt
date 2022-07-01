package io.paddle.idea.ui

import com.intellij.openapi.externalSystem.service.task.ui.AbstractExternalSystemToolWindowFactory
import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings
import com.intellij.openapi.project.Project
import io.paddle.idea.PaddleManager
import io.paddle.idea.settings.PaddleSettings

class PaddleToolWindowFactory : AbstractExternalSystemToolWindowFactory(PaddleManager.ID) {
    override fun getSettings(project: Project): AbstractExternalSystemSettings<*, *, *> {
        return PaddleSettings.getInstance(project)
    }
}
