package io.paddle.idea.settings

import com.intellij.openapi.externalSystem.settings.ExternalProjectSettings
import com.intellij.openapi.util.Key

class PaddleExternalProjectSettings(private val paddleYamlPath: String) : ExternalProjectSettings() {
    companion object {
        val KEY = Key<PaddleExternalProjectSettings>("paddle.external-project-settings-key")
    }

    override fun getExternalProjectPath(): String {
        return paddleYamlPath
    }

    override fun clone(): ExternalProjectSettings {
        return this.clone()
    }
}
