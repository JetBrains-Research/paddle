package io.paddle.idea.execution.marker

import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyFunction
import io.paddle.project.PaddleProject

object PyTestFunctionFinder : PyTestTargetFinder() {
    override fun checkTarget(element: PsiElement, project: PaddleProject, target: List<String>): Boolean {
        require(element is PyFunction)
        if (target.size == 1) {
            return false
        }
        val targetFunctionName = target.last()
        val psiFunctionName = element.name ?: return false
        val parent = element.parent ?: return false
        return targetFunctionName == psiFunctionName && check(parent, project, target.dropLast(1))
    }
}
