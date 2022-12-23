package io.paddle.idea.copypaste.poetry

import com.intellij.openapi.project.Project
import io.paddle.idea.copypaste.common.ConverterType
import io.paddle.idea.copypaste.common.PasteDialogBase

class PasteFromPoetryDialog(project: Project) : PasteDialogBase(project, "pyproject.toml", ConverterType.Poetry) {
    init {
        init()
    }
}
