package io.paddle.idea.utils

import io.paddle.project.Project
import io.paddle.terminal.TextOutput
import java.io.File

object PaddleProject {
    var currentProject: Project? = null

    fun load(file: File, workDir: File, output: TextOutput = TextOutput.Console): Project {
        return Project.load(file, workDir, output = output).also { currentProject = it }
    }
}
