package io.paddle.idea.copypaste.poetry

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import io.paddle.idea.settings.global.PaddleAppSettings

class PasteFromPoetryDialog(project: Project) : DialogWrapper(project, true) {
    init {
        isModal = true
        title = "Convert pyproject.toml"
        init()
    }

    override fun createCenterPanel() = panel {
        row {
            label("Clipboard content is copied from pyproject.toml file. Do you want to convert it to Paddle configuration YAML?")
        }
        row {
            checkBox("Don't show this dialog next time")
                .bindSelected(PaddleAppSettings.getInstance()::isDontShowDialogOnPoetryPaste)
        }
    }
}
