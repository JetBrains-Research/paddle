package io.paddle.idea

import com.intellij.openapi.components.Service
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.settings.*
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic

val PADDLE_ID: ProjectSystemId = ProjectSystemId("Paddle")


class PaddleExternalProjectSettings(val path: String) : ExternalProjectSettings() {
    override fun getExternalProjectPath(): String {
        return path
    }

    override fun clone(): ExternalProjectSettings {
        return this.clone()
    }
}

open class PaddleSettingsListenerDelegatingSettingsAdapter(delegate: ExternalSystemSettingsListener<PaddleExternalProjectSettings>) :
    DelegatingExternalSystemSettingsListener<PaddleExternalProjectSettings>(delegate), PaddleExternalProjectSettingsListener {
}

class PaddleExternalSystemSettings(project: Project) :
    AbstractExternalSystemSettings<PaddleExternalSystemSettings, PaddleExternalProjectSettings, PaddleExternalProjectSettingsListener>(
        PaddleExternalProjectSettingsListener.TOPIC,
        project
    ) {
    override fun subscribe(listener: ExternalSystemSettingsListener<PaddleExternalProjectSettings>) {
        project.messageBus.connect().subscribe(changesTopic, PaddleSettingsListenerDelegatingSettingsAdapter(listener))
    }

    override fun copyExtraSettingsFrom(settings: PaddleExternalSystemSettings) {
    }

    override fun checkSettings(old: PaddleExternalProjectSettings, current: PaddleExternalProjectSettings) {
    }
}

interface PaddleExternalProjectSettingsListener : ExternalSystemSettingsListener<PaddleExternalProjectSettings> {
    companion object {
        @JvmField
        val TOPIC: Topic<PaddleExternalProjectSettingsListener> =
            Topic.create("Paddle project specific settings", PaddleExternalProjectSettingsListener::class.java)
    }

}

@Service
class PaddleLocalSettings(project: Project) : AbstractExternalSystemLocalSettings<AbstractExternalSystemLocalSettings.State>(PADDLE_ID, project) {
}

