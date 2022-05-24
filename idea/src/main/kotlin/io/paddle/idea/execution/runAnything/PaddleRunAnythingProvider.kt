package io.paddle.idea.execution.runAnything

import com.intellij.execution.Executor
import com.intellij.ide.actions.runAnything.*
import com.intellij.ide.actions.runAnything.activity.RunAnythingCommandLineProvider
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.project.Project
import io.paddle.idea.PaddleManager
import io.paddle.idea.settings.PaddleSettings

/**
 * "Run Anything..." action is disabled in PyCharm IDE.
 *
 * See and vote: https://youtrack.jetbrains.com/issue/PY-31773/Implement-Run-Anything-for-PyCharm
 */
class PaddleRunAnythingProvider : RunAnythingCommandLineProvider() {
    companion object {
        const val HELP_COMMAND = "paddle"
    }

    override fun getHelpCommand(): String = HELP_COMMAND

    override fun run(dataContext: DataContext, commandLine: CommandLine): Boolean {
        val project = RunAnythingUtil.fetchProject(dataContext)
        val executionContext = dataContext.getData(EXECUTING_CONTEXT) ?: RunAnythingContext.ProjectContext(project)
        val context = createContext(project, executionContext, dataContext) ?: return false
        PaddleExecuteTaskAction.runPaddle(project, context.executor, context.workingDirectory, commandLine.command)
        return true
    }

    private fun createContext(project: Project, context: RunAnythingContext, dataContext: DataContext): Context? {
        val workingDirectory = context.getWorkingDirectory() ?: return null
        val executor = RunAnythingAction.EXECUTOR_KEY.getData(dataContext)
        return Context(context, project, workingDirectory, executor)
    }

    private fun RunAnythingContext.getWorkingDirectory(): String? {
        return when (this) {
            is RunAnythingContext.ProjectContext -> getLinkedProjectPath() ?: getPath()
            is RunAnythingContext.ModuleContext -> getLinkedModulePath() ?: getPath()
            else -> getPath()
        }
    }

    private fun RunAnythingContext.ProjectContext.getLinkedProjectPath(): String? {
        return PaddleSettings.getInstance(project)
            .linkedProjectsSettings.firstOrNull()
            ?.let { ExternalSystemApiUtil.findProjectData(project, PaddleManager.ID, it.externalProjectPath) }
            ?.data?.linkedExternalProjectPath
    }

    private fun RunAnythingContext.ModuleContext.getLinkedModulePath(): String? {
        return ExternalSystemApiUtil.getExternalProjectPath(module)
    }

    override fun suggestCompletionVariants(dataContext: DataContext, commandLine: CommandLine): Sequence<String> {
        return sequenceOf() // TODO
    }

    data class Context(
        val context: RunAnythingContext,
        val project: Project,
        val workingDirectory: String,
        val executor: Executor?
    )
}
