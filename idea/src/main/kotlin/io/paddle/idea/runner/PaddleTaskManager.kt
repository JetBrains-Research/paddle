package io.paddle.idea.runner

import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager
import io.paddle.idea.settings.PaddleExecutionSettings
import io.paddle.idea.utils.PaddleProject
import io.paddle.idea.utils.findPaddleInDirectory
import io.paddle.terminal.TextOutput
import java.io.File
import kotlin.io.path.Path

class PaddleTaskManager : ExternalSystemTaskManager<PaddleExecutionSettings> {
    override fun executeTasks(
        id: ExternalSystemTaskId,
        taskNames: MutableList<String>,
        projectPath: String,
        settings: PaddleExecutionSettings?,
        jvmParametersSetup: String?,
        listener: ExternalSystemTaskNotificationListener
    ) {
        val file = Path(projectPath).findPaddleInDirectory()!!.toFile()
        val project = PaddleProject.load(file, File(projectPath), object : TextOutput{
            override fun output(text: String) {
                listener.onTaskOutput(id, text, true)
            }
        })
        for (task in taskNames) {
            project.execute(task)
        }
        super.executeTasks(id, taskNames, projectPath, settings, jvmParametersSetup, listener)
    }

    override fun cancelTask(id: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean = false
}
