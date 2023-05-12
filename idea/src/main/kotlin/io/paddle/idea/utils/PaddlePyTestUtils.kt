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
import kotlin.io.path.Path

fun getProject(context: ConfigurationContext): PaddleProject? {
    val module = context.location?.module ?: return null
    val moduleDir = module.basePath?.let { File(it) } ?: return null
    val rootDir = context.project.basePath?.let { File(it) } ?: return null

    return PaddleProjectProvider.getInstance(rootDir).getProject(moduleDir)
}

private fun checkTargetForFile(file: PyFile, project: PaddleProject, target: List<String>): Boolean {
    if (target.size != 1 || !target.first().endsWith(".py")) {
        return false
    }
    val path = file.virtualFile.path
    val targetPath = project.roots.tests.resolve(target.first())
    return targetPath.exists() && targetPath.isFile && Path(path) == targetPath.toPath()
}

private fun checkTargetForDirectory(directory: PsiDirectory, project: PaddleProject, target: List<String>): Boolean {
    if (target.size != 1) {
        return false
    }
    val path = directory.virtualFile.path
    val targetPath = project.roots.tests.resolve(target.first())
    return targetPath.exists() && targetPath.isDirectory && Path(path) == targetPath.toPath()
}

private fun checkTargetForFunction(function: PyFunction, project: PaddleProject, target: List<String>): Boolean {
    if (target.size == 1) {
        return false
    }
    val targetFunctionName = target.last()
    val psiFunctionName = function.name ?: return false
    val parent = function.parent ?: return false
    return targetFunctionName == psiFunctionName && checkTargetForElement(parent, project, target.dropLast(1))
}

private fun checkTargetForClass(clazz: PyClass, project: PaddleProject, target: List<String>): Boolean {
    if (target.size == 1) {
        return false
    }
    val className = clazz.name ?: return false
    val targetClassName = target.last()
    val parent = clazz.parent ?: return false
    return className == targetClassName && checkTargetForElement(parent, project, target.dropLast(1))
}

private fun checkTargetForElement(element: PsiElement, project: PaddleProject, target: List<String>): Boolean {
    if (target.isEmpty()) {
        return element is PsiDirectory
    }

    return when (element) {
        is PyFile -> checkTargetForFile(element, project, target)
        is PsiDirectory -> checkTargetForDirectory(element, project, target)
        is PyFunction -> checkTargetForFunction(element, project, target)
        is PyClass -> checkTargetForClass(element, project, target)
        is PyStatementList -> { // PyClass -> PyStatementList -> PyFunction
            val parent = element.parent ?: return false
            if (parent !is PyClass) return false
            checkTargetForClass(parent, project, target)
        }
        else -> false
    }
}

private fun tryFindForParent(element: PsiElement, context: ConfigurationContext): Map<String, Any>? {
    val project = getProject(context) ?: return null // TODO: speed up
    return when (element) {
        is PsiDirectory -> {
            val path = element.virtualFile.path
            if (Path(path) == project.roots.tests.toPath()) {
                null
            } else {
                val parent = element.parent ?: return null
                findTestTaskForElement(parent, context)
            }
        }

        is PyClass, is PyFile, is PyFunction -> {
            val parent = element.parent ?: return null
            findTestTaskForElement(parent, context)
        }

        else -> null
    }
}

fun findTestTaskForElement(element: PsiElement, context: ConfigurationContext): Map<String, Any>? {
    val project = getProject(context) ?: return null
    val testTasks = project.config.get<List<Map<String, Any>>?>("tasks.test.pytest") ?: return null

    return testTasks.find {
        val targets = it["targets"] as List<String>? ?: listOf(project.roots.tests.path)
        targets.any { target ->
            checkTargetForElement(element, project, target.split("::"))
        }
    } ?: tryFindForParent(element, context)
}
