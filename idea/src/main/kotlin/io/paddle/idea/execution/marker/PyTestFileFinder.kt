package io.paddle.idea.execution.marker

import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyFile
import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.PaddleProject
import kotlin.io.path.Path

object PyTestFileFinder : PyTestTargetFinder() {
    override fun checkTarget(element: PsiElement, project: PaddleProject, target: List<String>): Boolean {
        require(element is PyFile)
        if (target.size != 1 || !target.first().endsWith(".py")) {
            return false
        }
        val path = element.virtualFile.path
        val targetPath = project.roots.tests.resolve(target.first())
        return targetPath.exists() && targetPath.isFile && Path(path) == targetPath.toPath()
    }
}
