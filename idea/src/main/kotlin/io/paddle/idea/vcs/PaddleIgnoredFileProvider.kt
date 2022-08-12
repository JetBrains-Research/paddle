package io.paddle.idea.vcs

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.FilePath
import com.intellij.openapi.vcs.changes.*

class PaddleIgnoredFileProvider : IgnoredFileProvider {
    override fun isIgnoredFile(project: Project, filePath: FilePath): Boolean =
        when {
            filePath.isDirectory && filePath.name == ".paddle" -> true
            !filePath.isDirectory && filePath.name == "paddle.auth.yaml" -> true
            else -> false
        }

    override fun getIgnoredFiles(project: Project) = setOf<IgnoredFileDescriptor>(
        IgnoredBeanFactory.withMask("**/.paddle/**"),
        IgnoredBeanFactory.withMask("paddle.auth.yaml"),
    )

    override fun getIgnoredGroupDescription() = "Paddle ignored files"
}
