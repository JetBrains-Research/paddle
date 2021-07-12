package io.paddle.idea.ui

import com.intellij.openapi.externalSystem.service.task.ui.AbstractExternalSystemToolWindowFactory
import io.paddle.idea.PaddleManager

class PaddleToolWindowFactory : AbstractExternalSystemToolWindowFactory(PaddleManager.ID) {
}
