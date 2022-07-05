package io.paddle.idea.execution

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import io.paddle.idea.utils.getSuperParent

class PaddleRunLineMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (element.containingFile.name != "paddle.yaml") return null
        var shouldRenderRunLineMarker = false

        // Run general task
        shouldRenderRunLineMarker = shouldRenderRunLineMarker ||
            (element.text.contains("id")
                && element.getSuperParent(5)?.text?.startsWith("run") ?: false
                && element.getSuperParent(7)?.text?.startsWith("tasks") ?: false)

        // Run PyTest
        shouldRenderRunLineMarker = shouldRenderRunLineMarker ||
            (element.text.contains("id")
                && element.getSuperParent(5)?.text?.startsWith("pytest") ?: false
                && element.getSuperParent(7)?.text?.startsWith("tasks") ?: false)

        if (shouldRenderRunLineMarker) {
            val actions = ExecutorAction.getActions(Integer.MAX_VALUE)
            return Info(AllIcons.RunConfigurations.TestState.Run, actions) { e ->
                actions.mapNotNull { getText(it, e) }.joinToString("\n")
            }
        }

        return null
    }
}
