package io.paddle.idea.settings

import com.intellij.openapi.externalSystem.settings.DelegatingExternalSystemSettingsListener
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListener

open class PaddleSettingsListenerDelegatingSettingsAdapter(delegate: ExternalSystemSettingsListener<PaddleExternalProjectSettings>) :
    DelegatingExternalSystemSettingsListener<PaddleExternalProjectSettings>(delegate),
    PaddleExternalProjectSettingsListener {
}
