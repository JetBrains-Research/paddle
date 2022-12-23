package io.paddle.idea.copypaste.requirements

import com.intellij.openapi.project.Project
import io.paddle.idea.copypaste.common.ConverterType
import io.paddle.idea.copypaste.common.PasteDialogBase

class PasteFromRequirementsTxtDialog(project: Project) : PasteDialogBase(project, "requirements.txt", ConverterType.RequirementsTxt) {
    init {
        init()
    }
}
