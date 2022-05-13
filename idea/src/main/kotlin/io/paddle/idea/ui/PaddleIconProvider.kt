package io.paddle.idea.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.externalSystem.ui.ExternalSystemIconProvider
import javax.swing.Icon

class PaddleIconProvider : ExternalSystemIconProvider {
    override val reloadIcon: Icon = AllIcons.Actions.BuildLoadChanges
}
