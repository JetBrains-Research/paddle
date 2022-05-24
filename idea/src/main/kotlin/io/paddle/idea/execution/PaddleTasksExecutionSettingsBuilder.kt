package io.paddle.idea.execution

import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings
import com.intellij.openapi.project.Project
import com.intellij.task.ProjectTask
import io.paddle.idea.PaddleManager
import java.io.File

class PaddleTasksExecutionSettingsBuilder(val project: Project, val tasks: List<ProjectTask>) {
    fun build(rootProjectPath: String): ExternalSystemTaskExecutionSettings {
        val settings = ExternalSystemTaskExecutionSettings()
        val projectFile = File(rootProjectPath)
        val projectName = if (projectFile.isFile) {
            projectFile.parentFile.name
        } else {
            projectFile.name
        }
        val executionName = "Build $projectName"

        settings.executionName = executionName
        settings.externalProjectPath = rootProjectPath
        settings.taskNames = tasks.map { it.presentableName } // FIXME? build + clean tasks in Gradle
        settings.vmOptions = ""
        settings.externalSystemIdString = PaddleManager.ID.id

        return settings
    }
}
