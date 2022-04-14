package io.paddle.idea.utils

import io.paddle.project.PaddleProject
import io.paddle.project.PaddleProjectProvider
import io.paddle.terminal.TextOutput
import java.io.File

@Deprecated("Replaced with [io.paddle.project.PaddleProjectProvider]")
object PaddleProjectIdeaProvider {
    var currentProject: PaddleProject? = null

    fun load(workDir: File, output: TextOutput = TextOutput.Console): PaddleProject {
        return PaddleProjectProvider.getInstance(workDir).initializeProject(output).also { currentProject = it }
    }
}
