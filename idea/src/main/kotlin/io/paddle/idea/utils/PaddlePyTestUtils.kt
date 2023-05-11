package io.paddle.idea.utils

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.*
import com.jetbrains.python.sdk.basePath
import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.PaddleProject
import io.paddle.project.PaddleProjectProvider
import java.io.File

fun getProject(context: ConfigurationContext): PaddleProject? {
    val module = context.location?.module ?: return null
    val moduleDir = module.basePath?.let { File(it) } ?: return null
    val rootDir = context.project.basePath?.let { File(it) } ?: return null

    return PaddleProjectProvider.getInstance(rootDir).getProject(moduleDir)
}

private fun findTestTaskForFile(element: PyFile, context: ConfigurationContext): Map<String, Any>? {
    val directory = element.parent ?: return null
    return findTestTaskForElement(directory, context)
}
private fun findTestTaskForDirectory(element: PsiDirectory, context: ConfigurationContext): Map<String, Any>? {
    val project = getProject(context) ?: return null
    val testTasks = project.config.get<List<Map<String, Any>>?>("tasks.test.pytest") ?: return null
    val path = element.virtualFile.path
    return testTasks.find {
            val targets = it["targets"] as List<String>? ?: listOf("/")
        targets.any { target ->
            val targetPath = if (target == "/") project.roots.tests.path else project.roots.tests.resolve(target).path
            targetPath == path
        }
    }
}

private fun findTestTaskForFunction(element: PyFunction, context: ConfigurationContext): Map<String, Any>? = findTestTaskForElement(element.parent, context)
private fun findTestTaskForClass(element: PyClass, context: ConfigurationContext): Map<String, Any>? = null

fun findTestTaskForElement(element: PsiElement, context: ConfigurationContext): Map<String, Any>? = when (element) {
    is PyFile -> findTestTaskForFile(element, context)
    is PsiDirectory -> findTestTaskForDirectory(element, context)
    is PyFunction -> findTestTaskForFunction(element, context)
    is PyClass -> findTestTaskForClass(element, context)
    else -> null
}