package io.paddle.idea.execution.marker

import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyClass
import io.paddle.project.PaddleProject

object PyTestClassFinder : PyTestTargetFinder() {
    override fun checkTarget(element: PsiElement, project: PaddleProject, target: List<String>): Boolean {
        require(element is PyClass)
        if (target.size == 1) {
            return false
        }
        val className = element.name ?: return false
        val targetClassName = target.last()
        val parent = element.parent ?: return false
        return className == targetClassName && check(parent, project, target.dropLast(1))
    }
}
