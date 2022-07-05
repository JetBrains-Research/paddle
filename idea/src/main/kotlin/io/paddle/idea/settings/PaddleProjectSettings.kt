package io.paddle.idea.settings

import com.intellij.openapi.externalSystem.settings.*
import com.intellij.util.messages.Topic

class PaddleProjectSettings : ExternalProjectSettings() {
    interface Listener : ExternalSystemSettingsListener<PaddleProjectSettings> {
        abstract class Adapter : Listener, ExternalSystemSettingsListenerAdapter<PaddleProjectSettings>()

        class DelegatingAdapter(delegate: ExternalSystemSettingsListener<PaddleProjectSettings>) :
            DelegatingExternalSystemSettingsListener<PaddleProjectSettings>(delegate), Listener

        companion object {
            @JvmField
            val TOPIC: Topic<Listener> = Topic.create("Paddle project specific settings", Listener::class.java)
        }
    }


    override fun clone(): ExternalProjectSettings {
        val result = PaddleProjectSettings()
        copyTo(result)
        return result
    }
}
