package io.paddle.idea.settings

import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings

class PaddleExecutionSettings : ExternalSystemExecutionSettings() {
    override fun isVerboseProcessing(): Boolean {
        return true
    }
}
