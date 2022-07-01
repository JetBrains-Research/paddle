package io.paddle.idea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import io.paddle.project.PaddleProject
import io.paddle.project.PaddleProjectProvider
import java.io.File

fun CompletionParameters.extractPaddleProject(): PaddleProject? {
    val rootDir = editor.project?.basePath?.let { File(it) }
    val virtualFile = originalFile.virtualFile?.takeIf { it.name == "paddle.yaml" }

    // FIXME: not sure if it is ok to go from [VirtualFile] to [File] like that. how to fix it?
    val workDir = virtualFile?.parent?.canonicalPath?.let { File(it) }

    return rootDir?.let { _rootDir ->
        workDir?.let { _workDir ->
            PaddleProjectProvider.getInstance(_rootDir).getProject(_workDir)
        }
    }
}
