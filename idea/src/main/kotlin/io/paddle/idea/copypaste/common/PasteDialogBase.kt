package io.paddle.idea.copypaste.common

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import io.paddle.idea.copypaste.poetry.PasteFromPoetryDialog
import io.paddle.idea.copypaste.requirements.PasteFromRequirementsTxtDialog
import io.paddle.idea.settings.global.PaddleAppSettings

abstract class PasteDialogBase(project: Project, private val fileName: String, private val converterType: ConverterType) : DialogWrapper(project, true) {
    init {
        isModal = true
        title = "Convert $fileName"
    }

    override fun createCenterPanel() = panel {
        row {
            label("Clipboard content is copied from $fileName file. Do you want to convert it to Paddle configuration YAML?")
        }
        row {
            checkBox("Don't show this dialog next time")
                .bindSelected(PaddleAppSettings.getDontShowDialogOnPasteBind(converterType))
        }
    }

    companion object {
        fun create(project: Project, converterType: ConverterType): PasteDialogBase {
            return when (converterType) {
                ConverterType.Poetry -> PasteFromPoetryDialog(project)
                ConverterType.RequirementsTxt -> PasteFromRequirementsTxtDialog(project)
            }
        }
    }
}
