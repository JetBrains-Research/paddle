package io.paddle.idea.copypaste.requirements

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import io.paddle.idea.settings.global.PaddleAppSettings

class PasteFromRequirementsTxtDialog(project: Project) : DialogWrapper(project, true) {
    init {
        isModal = true
        title = "Convert requirements.txt"
        init()
    }

    override fun createCenterPanel() = panel {
        row {
            label("Clipboard content is copied from requirements.txt file. Do you want to convert it to Paddle configuration YAML?")
        }
        row {
            checkBox("Don't show this dialog next time")
                .bindSelected(PaddleAppSettings.getInstance()::isDontShowDialogOnRequirementTxtPaste)
        }
    }
}
