package io.paddle.idea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import io.paddle.project.PaddleProject
import io.paddle.project.PaddleProjectProvider
import java.io.File

fun CompletionParameters.extractPaddleProject(): PaddleProject? {
    val rootDir = editor.project?.basePath?.let { File(it) }
    val workDir = originalFile.virtualFile?.parent?.toNioPath()?.toFile()

    return rootDir?.let { _rootDir ->
        workDir?.let { _workDir ->
            PaddleProjectProvider.getInstance(_rootDir).getProject(_workDir)
        }
    }
}
