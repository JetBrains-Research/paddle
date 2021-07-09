package io.paddle.idea.settings

import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListenerAdapter
import com.intellij.util.messages.Topic

interface PaddleExternalProjectSettingsListener : ExternalSystemSettingsListener<PaddleExternalProjectSettings> {
    abstract class Adapter: PaddleExternalProjectSettingsListener, ExternalSystemSettingsListenerAdapter<PaddleExternalProjectSettings>()

    companion object {
        @JvmField
        val TOPIC: Topic<PaddleExternalProjectSettingsListener> =
            Topic.create("Paddle project specific settings", PaddleExternalProjectSettingsListener::class.java)
    }

}
