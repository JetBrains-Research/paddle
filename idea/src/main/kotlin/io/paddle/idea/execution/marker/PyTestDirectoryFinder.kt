package io.paddle.idea.execution.marker

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.PaddleProject
import io.paddle.utils.config.PaddleAppRuntime
import java.nio.file.Path
import kotlin.io.path.Path

object PyTestDirectoryFinder : PyTestTargetFinder() {
    override fun checkTarget(element: PsiElement, project: PaddleProject, target: List<String>): Boolean {
        require(element is PsiDirectory)
        if (target.size != 1) {
            return false
        }
        val path = element.getPath()
        val targetPath = project.roots.tests.resolve(target.first())
        return targetPath.exists() && targetPath.isDirectory && path == targetPath.toPath()
    }

    private fun PsiDirectory.getPath(): Path {
        val pathString = virtualFile.path
        return if (PaddleAppRuntime.isTests && pathString.startsWith("/")) Path(pathString.drop(1)) else Path(pathString)
    }

}
