package io.paddle.idea.execution

import com.intellij.build.BuildViewManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.task.TaskCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.task.*
import io.paddle.idea.PaddleManager
import org.jetbrains.concurrency.AsyncPromise
import org.jetbrains.concurrency.Promise
import java.util.concurrent.atomic.AtomicInteger

@Suppress("UnstableApiUsage")
class PaddleProjectTaskRunner : ProjectTaskRunner() {
    private val logger = Logger.getInstance(PaddleProjectTaskRunner::class.java)

    override fun canRun(projectTask: ProjectTask): Boolean = true

    override fun run(project: Project, context: ProjectTaskContext, vararg tasks: ProjectTask): Promise<Result> {
        val resultPromise = AsyncPromise<Result>()
        val executionSettingsBuilder = PaddleTasksExecutionSettingsBuilder(project, tasks.toList())
        val rootProjectPath = project.basePath
            ?: return resultPromise.also {
                logger.warn("Nothing will be run for: " + tasks.contentToString())
                resultPromise.setResult(TaskRunnerResults.SUCCESS)
            }
        val settings = executionSettingsBuilder.build(rootProjectPath)
        val userData = UserDataHolderBase()
        userData.putUserData(ExternalSystemRunConfiguration.PROGRESS_LISTENER_KEY, BuildViewManager::class.java)

        val errorCounter = AtomicInteger()

        val taskCallback = object : TaskCallback {
            override fun onSuccess() {
                handle(true)
            }

            override fun onFailure() {
                handle(false)
            }

            private fun handle(success: Boolean) {
                val errors: Int = if (success) errorCounter.get() else errorCounter.incrementAndGet()
                resultPromise.setResult(if (errors > 0) TaskRunnerResults.FAILURE else TaskRunnerResults.SUCCESS)
            }
        }

        ExternalSystemUtil.runTask(
            settings, DefaultRunExecutor.EXECUTOR_ID, project, PaddleManager.ID,
            taskCallback, ProgressExecutionMode.IN_BACKGROUND_ASYNC, false, userData
        )

        return resultPromise
    }
}

