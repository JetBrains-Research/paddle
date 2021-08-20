package io.paddle.idea.settings

import com.intellij.openapi.externalSystem.service.settings.AbstractExternalProjectSettingsControl
import com.intellij.openapi.externalSystem.service.settings.AbstractExternalSystemConfigurable
import com.intellij.openapi.externalSystem.util.ExternalSystemSettingsControl
import com.intellij.openapi.externalSystem.util.PaintAwarePanel
import com.intellij.openapi.project.Project
import io.paddle.idea.PaddleManager
import org.jetbrains.annotations.NonNls


class PaddleConfigurable(project: Project) :
    AbstractExternalSystemConfigurable<PaddleProjectSettings, PaddleProjectSettings.Listener, PaddleSettings>(project, PaddleManager.ID) {

    override fun createProjectSettingsControl(settings: PaddleProjectSettings): ExternalSystemSettingsControl<PaddleProjectSettings> {
        return object : AbstractExternalProjectSettingsControl<PaddleProjectSettings>(settings) {
            override fun validate(settings: PaddleProjectSettings): Boolean {
                return true
            }

            override fun fillExtraControls(content: PaintAwarePanel, indentLevel: Int) {
            }

            override fun isExtraSettingModified(): Boolean {
                return false
            }

            override fun resetExtraSettings(isDefaultModuleCreation: Boolean) {
            }

            override fun applyExtraSettings(settings: PaddleProjectSettings) {
            }
        }
    }

    override fun createSystemSettingsControl(settings: PaddleSettings): ExternalSystemSettingsControl<PaddleSettings>? {
        return null
    }

    override fun newProjectSettings(): PaddleProjectSettings {
        return PaddleProjectSettings()
    }

    override fun getId(): String {
        return ID
    }

    override fun getHelpTopic(): String {
        return HELP_TOPIC!!
    }

    companion object {
        const val ID = "reference.settingsdialog.project.paddle"
        val HELP_TOPIC: @NonNls String = ID
    }
}
