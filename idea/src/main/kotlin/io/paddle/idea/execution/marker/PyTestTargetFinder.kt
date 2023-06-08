package io.paddle.idea.execution.marker

import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.*
import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.PaddleProject
import kotlin.io.path.Path

sealed class PyTestTargetFinder {
    abstract fun checkTarget(element: PsiElement, project: PaddleProject, target: List<String>): Boolean

    companion object {
        @JvmStatic
        protected fun check(element: PsiElement, project: PaddleProject, target: List<String>): Boolean = when (element) {
            is PsiDirectory -> PyTestDirectoryFinder.checkTarget(element, project, target)
            is PyFile -> PyTestFileFinder.checkTarget(element, project, target)
            is PyFunction -> PyTestFunctionFinder.checkTarget(element, project, target)
            is PyClass -> PyTestClassFinder.checkTarget(element, project, target)
            is PyStatementList -> PyTestStatementListFinder.checkTarget(element, project, target)
            else -> false
        }
        fun findTestTaskForElement(element: PsiElement, project: PaddleProject): Map<String, Any>? {
            val testTasks = project.config.get<List<Map<String, Any>>?>("tasks.test.pytest") ?: return null

            return testTasks.find {
                val targets = it["targets"] as List<String>? ?: listOf(project.roots.tests.path)
                targets.any { target ->
                    check(element, project, target.split("::"))
                }
            } ?: tryFindForParent(element, project)
        }

        private fun tryFindForParent(element: PsiElement, project: PaddleProject): Map<String, Any>? {
            return when (element) {
                is PsiDirectory -> {
                    val path = element.virtualFile.path
                    if (Path(path) == project.roots.tests.toPath()) {
                        null
                    } else {
                        val parent = element.parent ?: return null
                        findTestTaskForElement(parent, project)
                    }
                }

                is PyClass, is PyFile, is PyFunction -> {
                    val parent = element.parent ?: return null
                    findTestTaskForElement(parent, project)
                }
                else -> null
            }
        }
    }
}
