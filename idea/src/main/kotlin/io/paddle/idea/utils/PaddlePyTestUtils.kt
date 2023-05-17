package io.paddle.idea.utils

import com.intellij.execution.actions.ConfigurationContext
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




