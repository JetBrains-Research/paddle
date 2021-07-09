package io.paddle.idea.ui

import com.intellij.openapi.externalSystem.service.task.ui.AbstractExternalSystemToolWindowFactory
import io.paddle.idea.PaddleExternalSystemManager

class PaddleToolWindowFactory : AbstractExternalSystemToolWindowFactory(PaddleExternalSystemManager.ID) {
}
