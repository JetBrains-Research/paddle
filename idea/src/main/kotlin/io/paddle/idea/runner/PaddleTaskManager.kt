package io.paddle.idea.runner

import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager
import io.paddle.idea.settings.PaddleExecutionSettings
import io.paddle.idea.utils.*
import io.paddle.tasks.Task
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.io.path.Path

class PaddleTaskManager : ExternalSystemTaskManager<PaddleExecutionSettings> {
    companion object {
        private val logger = LoggerFactory.getLogger(PaddleTaskManager::class.java)
    }

    override fun executeTasks(
        id: ExternalSystemTaskId,
        taskNames: MutableList<String>,
        projectPath: String,
        settings: PaddleExecutionSettings?,
        jvmParametersSetup: String?,
        listener: ExternalSystemTaskNotificationListener
    ) {
        val file = Path(projectPath).findPaddleInDirectory()!!.toFile()
        val project = PaddleProject.load(file, File(projectPath), IDEACommandOutput(id, listener))

        for (task in taskNames) {
            listener.onStart(id, projectPath)
            try {
                project.execute(task)
            } catch (e: Task.ActException) {
                listener.onFailure(id, e)
                continue
            }
            listener.onSuccess(id)
            listener.onEnd(id)
        }
    }

    override fun cancelTask(id: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean = false
}
