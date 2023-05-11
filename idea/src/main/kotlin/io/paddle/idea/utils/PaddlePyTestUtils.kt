package io.paddle.idea.utils

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.*
import com.jetbrains.python.sdk.basePath
import io.paddle.project.PaddleProject
import io.paddle.project.PaddleProjectProvider
import java.io.File

fun getProject(context: ConfigurationContext): PaddleProject? {
    val module = context.location?.module ?: return null
    val moduleDir = module.basePath?.let { File(it) } ?: return null
    val rootDir = context.project.basePath?.let { File(it) } ?: return null

    return PaddleProjectProvider.getInstance(rootDir).getProject(moduleDir)
}

private fun findTestTaskForFile(element: PyFile): Map<String, Any>? = null
private fun findTestTaskForDirectory(element: PsiDirectory): Map<String, Any>? = null
private fun findTestTaskForFunction(element: PyFunction): Map<String, Any>? = null
private fun findTestTaskForClass(element: PyClass): Map<String, Any>? = null

fun findTestTaskForElement(element: PsiElement, context: ConfigurationContext): Map<String, Any>? = when (element) {
    is PyFile -> findTestTaskForFile(element)
    is PsiDirectory -> findTestTaskForDirectory(element)
    is PyFunction -> findTestTaskForFunction(element)
    is PyClass -> findTestTaskForClass(element)
    else -> null
}
