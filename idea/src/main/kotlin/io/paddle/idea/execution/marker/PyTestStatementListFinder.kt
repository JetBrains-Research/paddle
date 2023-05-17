package io.paddle.idea.execution.marker

import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyClass
import com.jetbrains.python.psi.PyStatementList
import io.paddle.project.PaddleProject

object PyTestStatementListFinder : PyTestTargetFinder() {
    override fun checkTarget(element: PsiElement, project: PaddleProject, target: List<String>): Boolean {
        require(element is PyStatementList)
        val parent = element.parent ?: return false
        if (parent !is PyClass) return false
        return PyTestClassFinder.checkTarget(parent, project, target)
    }
}
