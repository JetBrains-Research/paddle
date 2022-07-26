package io.paddle.idea.execution

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.ExternalSystemAutoImportAware
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectListener
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemRefreshStatus
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.model.task.*
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.service.internal.ExternalSystemProcessingManager
import com.intellij.openapi.externalSystem.service.internal.ExternalSystemResolveProjectTask
import com.intellij.openapi.externalSystem.service.project.ExternalProjectRefreshCallback
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.ProjectManager
import io.paddle.idea.PaddleManager
import io.paddle.idea.settings.PaddleExecutionSettings
import io.paddle.idea.utils.IDEACommandOutput
import io.paddle.idea.utils.containsPrefix
import io.paddle.plugin.python.utils.PaddleLogger
import io.paddle.project.PaddleProject
import io.paddle.project.PaddleProjectProvider
import io.paddle.tasks.Task
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference

class PaddleTaskManager : ExternalSystemTaskManager<PaddleExecutionSettings> {
    private val log = Logger.getInstance(PaddleTaskManager::class.java)
    private lateinit var latch: CountDownLatch

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
            ?: throw IllegalStateException("Could not find corresponding intellij project for path $projectPath")
        val rootDir = project.basePath?.let { File(it) } ?: return
        val paddleProject = PaddleProjectProvider.getInstance(rootDir).getProject(workDir) ?: return

        if (paddleProject.isUpToDate) {
            executeTask(id, taskNames, paddleProject, projectPath, listener)
        } else {
            val importSpec = ImportSpecBuilder(project, PaddleManager.ID)
                .use(ProgressExecutionMode.START_IN_FOREGROUND_ASYNC)
                .callback(object : ExternalProjectRefreshCallback {
                    override fun onSuccess(externalTaskId: ExternalSystemTaskId, externalProject: DataNode<ProjectData>?) {
                        val paddleProject = PaddleProjectProvider.getInstance(rootDir).getProject(workDir) ?: return
                        executeTask(id, taskNames, paddleProject, projectPath, listener)
                        latch.countDown()
                    }

                    override fun onFailure(externalTaskId: ExternalSystemTaskId, errorMessage: String, errorDetails: String?) {
                        val paddleProject = PaddleProjectProvider.getInstance(rootDir).getProject(workDir) ?: return
                        paddleProject.output = IDEACommandOutput(externalTaskId, listener)
                        paddleProject.terminal.error(errorMessage)
                        latch.countDown()
                    }

                    override fun onSuccess(externalProject: DataNode<ProjectData>?) {
                        latch.countDown()
                    }

                    override fun onFailure(errorMessage: String, errorDetails: String?) {
                        latch.countDown()
                    }
                })
            latch = CountDownLatch(1)
            ExternalSystemUtil.refreshProject(rootDir.absolutePath, importSpec)
            latch.await()
        }
    }

    private fun executeTask(
        id: ExternalSystemTaskId,
        taskNames: MutableList<String>,
        paddleProject: PaddleProject,
        projectPath: String,
        listener: ExternalSystemTaskNotificationListener
    ) {
        paddleProject.output = IDEACommandOutput(id, listener)
        PaddleLogger.terminal = paddleProject.terminal

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

    private inner class TaskNotificationListener(
        val delegate: ExternalSystemProjectListener,
        val projectPath: String,
        val autoImportAware: ExternalSystemAutoImportAware
    ) : ExternalSystemTaskNotificationListenerAdapter() {
        var externalSystemTaskId = AtomicReference<ExternalSystemTaskId?>(null)

        override fun onStart(id: ExternalSystemTaskId, workingDir: String?) {
            if (id.type != ExternalSystemTaskType.RESOLVE_PROJECT) return
            val task = ApplicationManager.getApplication().getService(ExternalSystemProcessingManager::class.java).findTask(id)
            if (task is ExternalSystemResolveProjectTask) {
                if (!autoImportAware.isApplicable(task.resolverPolicy)) {
                    return
                }
            }
            externalSystemTaskId.set(id)
            delegate.onProjectReloadStart()
        }

        private fun afterProjectRefresh(id: ExternalSystemTaskId, status: ExternalSystemRefreshStatus) {
            if (id.type != ExternalSystemTaskType.RESOLVE_PROJECT) return
            if (!externalSystemTaskId.compareAndSet(id, null)) return
            delegate.onProjectReloadFinish(status)
        }

        override fun onSuccess(id: ExternalSystemTaskId) {
            afterProjectRefresh(id, ExternalSystemRefreshStatus.SUCCESS)
        }

        override fun onFailure(id: ExternalSystemTaskId, e: Exception) {
            afterProjectRefresh(id, ExternalSystemRefreshStatus.FAILURE)
        }

        override fun onCancel(id: ExternalSystemTaskId) {
            afterProjectRefresh(id, ExternalSystemRefreshStatus.CANCEL)
        }
    }

    override fun cancelTask(id: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean = false
}
