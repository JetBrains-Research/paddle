package io.paddle.idea.execution

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons.RunConfigurations
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.jetbrains.python.PyTokenTypes
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil
import com.jetbrains.python.psi.PyFile
import com.jetbrains.python.psi.PyUtil
import com.jetbrains.python.psi.impl.getIfStatementByIfKeyword

class PaddlePyRunLineMarkerContributor : RunLineMarkerContributor() {
    override fun getInfo(element: PsiElement): Info? {
        if (element.isMainClauseOnTopLevel()) {
            val actions: Array<AnAction> = ExecutorAction.getActions().take(1).toTypedArray()
            val tooltipProvider = { psiElement: PsiElement ->
                actions.mapNotNull {
                    getText(it, psiElement)
                }.joinToString(separator = "\n")
            }
            return object : Info(RunConfigurations.TestState.Red2, tooltipProvider, *actions) {
                override fun shouldReplace(other: Info): Boolean = true
            }
        }
        return null
    }

    override fun producesAllPossibleConfigurations(file: PsiFile): Boolean = true

    private fun PsiElement.isMainClauseOnTopLevel(): Boolean = when (node.elementType) {
        PyTokenTypes.IMPORT_KEYWORD -> true
        PyTokenTypes.IF_KEYWORD -> {
            val statement = getIfStatementByIfKeyword(this)
            statement != null && ScopeUtil.getScopeOwner(this) is PyFile && PyUtil.isIfNameEqualsMain(statement)
        }

        else -> false
    }

    companion object {

    }
}
