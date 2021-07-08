package io.paddle.idea

import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings

class PaddleExecutionSettings : ExternalSystemExecutionSettings() {
    override fun isVerboseProcessing(): Boolean {
        return true
    }
}
