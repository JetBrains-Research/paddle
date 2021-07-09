package io.paddle.idea.settings

import com.intellij.openapi.externalSystem.settings.AbstractExternalSystemSettings
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key

class PaddleExternalSystemSettings(project: Project) :
    AbstractExternalSystemSettings<PaddleExternalSystemSettings, PaddleExternalProjectSettings, PaddleExternalProjectSettingsListener>(
        PaddleExternalProjectSettingsListener.TOPIC,
        project
    ) {
    companion object {
        val KEY = Key<PaddleExternalSystemSettings>("paddle.external-system-settings-key")
    }


    override fun subscribe(listener: ExternalSystemSettingsListener<PaddleExternalProjectSettings>) {
        project.messageBus.connect().subscribe(changesTopic, PaddleSettingsListenerDelegatingSettingsAdapter(listener))
    }

    override fun copyExtraSettingsFrom(settings: PaddleExternalSystemSettings) {
    }

    override fun checkSettings(old: PaddleExternalProjectSettings, current: PaddleExternalProjectSettings) {
    }
}
