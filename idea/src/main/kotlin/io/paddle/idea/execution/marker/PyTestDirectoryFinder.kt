package io.paddle.idea.execution.marker

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.PaddleProject
import kotlin.io.path.Path

object PyTestDirectoryFinder : PyTestTargetFinder() {
    override fun checkTarget(element: PsiElement, project: PaddleProject, target: List<String>): Boolean {
        require(element is PsiDirectory)
        if (target.size != 1) {
            return false
        }
        val path = element.virtualFile.path
        val targetPath = project.roots.tests.resolve(target.first())
        return targetPath.exists() && targetPath.isDirectory && Path(path) == targetPath.toPath()
    }

}
