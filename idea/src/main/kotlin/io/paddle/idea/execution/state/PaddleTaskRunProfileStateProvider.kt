package io.paddle.idea.execution.state

import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.RunManagerEx
import com.intellij.execution.configurations.RunProfileState
import com.intellij.openapi.extensions.ExtensionPointName
import io.paddle.idea.execution.beforeRun.PaddleBeforeRunTaskProvider
import io.paddle.idea.execution.beforeRun.taskExists
import io.paddle.tasks.Task


internal interface PaddleTaskRunProfileStateProvider<T : Task> {
    fun getState(task: T, context: PaddleTaskRunProfileStateContext): RunProfileState?

    companion object {
        var EP_NAME: ExtensionPointName<PaddleTaskRunProfileStateProvider<*>> =
            ExtensionPointName.create("io.paddle.idea.runProfileStateProvider")

        fun <T : PaddleTaskRunProfileStateProvider<*>> findInstance(cls: Class<T>): T? {
            return EP_NAME.findExtension(cls)
        }
    }

    fun updateBeforeRunTasks(task: Task, context: PaddleTaskRunProfileStateContext) {
        val project = context.environment.project
        val beforeRunTaskProvider = BeforeRunTaskProvider.getProvider(project, PaddleBeforeRunTaskProvider.ID) as? PaddleBeforeRunTaskProvider
        val runManager = RunManagerEx.getInstanceEx(project)

        // If 1 -> 2, 1 -> 3, 2 -> 3 then we should execute only dep-task #2 as a before-run task for task #1
        // since dep-task #3 will be handled by Paddle automatically
        val deps = task.dependencies.filter { dep ->
            task.dependencies
                .filter { otherDep -> otherDep != dep }
                .all { otherDep -> !otherDep.executionOrder.contains(dep) }
        }

        val newBeforeRunTasks = deps.map { dependsOnTask ->
            beforeRunTaskProvider?.createTask(context.originalRunConfiguration)
                ?.apply {
                    taskExecutionSettings.externalProjectPath = dependsOnTask.project.workDir.absolutePath
                    taskExecutionSettings.taskNames = listOf(dependsOnTask.id)
                    isEnabled = true
                } ?: return
        }

        val oldBeforeRunTasks = runManager.getBeforeRunTasks(context.originalRunConfiguration)
        val beforeRunTasks = oldBeforeRunTasks + newBeforeRunTasks.filter { !oldBeforeRunTasks.taskExists(it) }

        runManager.setBeforeRunTasks(context.originalRunConfiguration, beforeRunTasks)
    }
}
