package io.paddle.idea.settings

import com.intellij.openapi.externalSystem.model.settings.ExternalSystemExecutionSettings
import java.io.File

class PaddleExecutionSettings(val rootDir: File) : ExternalSystemExecutionSettings() {
    override fun isVerboseProcessing(): Boolean {
        return true
    }
}
