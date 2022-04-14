package io.paddle.idea.runner

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskNotificationListener
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager
import com.intellij.openapi.project.ProjectManager
import io.paddle.idea.settings.PaddleExecutionSettings
import io.paddle.idea.utils.IDEACommandOutput
import io.paddle.idea.utils.containsPrefix
import io.paddle.project.PaddleDaemon
import io.paddle.tasks.Task
import java.io.File

class PaddleTaskManager : ExternalSystemTaskManager<PaddleExecutionSettings> {
    private val log = Logger.getInstance(PaddleTaskManager::class.java)

    override fun executeTasks(
        id: ExternalSystemTaskId,
        taskNames: MutableList<String>,
        projectPath: String,
        settings: PaddleExecutionSettings?,
        jvmParametersSetup: String?,
        listener: ExternalSystemTaskNotificationListener
    ) {
        val workDir = File(projectPath)
        val project = ProjectManager.getInstance().openProjects
            .find { it.isOpen && it.isInitialized && it.basePath != null && File(projectPath).containsPrefix(File(it.basePath!!)) }
            ?: run {
                log.warn("Could not find corresponding intellij project for path $projectPath")
                return
            }
        val rootDir = project.basePath?.let { File(it) } ?: return
        val paddleProject = PaddleDaemon.getInstance(rootDir).getProjectByWorkDir(workDir) ?: run {
            log.warn("Could not find corresponding paddle project for workDir ${workDir.canonicalPath}")
            return
        }
        paddleProject.output = IDEACommandOutput(id, listener)

        for (task in taskNames) {
            listener.onStart(id, projectPath)
            try {
                paddleProject.execute(task)
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
