package io.paddle.idea.utils

import io.paddle.project.Project
import io.paddle.project.ProjectProvider
import io.paddle.terminal.TextOutput
import java.io.File

@Deprecated("Replaced with DI via ProjectProvider")
object PaddleProjectIdeaProvider {
    var currentProject: Project? = null

    fun load(workDir: File, output: TextOutput = TextOutput.Console): Project {
        return ProjectProvider.getInstance(workDir).initializeProject(output).also { currentProject = it }
    }
}
