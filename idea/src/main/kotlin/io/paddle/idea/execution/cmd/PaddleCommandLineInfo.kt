package io.paddle.idea.execution.cmd

import com.intellij.icons.AllIcons
import com.intellij.openapi.externalSystem.service.ui.command.line.CommandLineInfo
import com.intellij.openapi.externalSystem.service.ui.command.line.CompletionTableInfo
import com.intellij.openapi.externalSystem.service.ui.completion.TextCompletionInfo
import com.intellij.openapi.externalSystem.service.ui.project.path.WorkingDirectoryField
import com.intellij.openapi.observable.properties.AtomicLazyProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ui.whenTextModified
import io.paddle.project.PaddleProjectProvider
import java.io.File
import javax.swing.Icon

class PaddleCommandLineInfo(project: Project, workingDirectoryField: WorkingDirectoryField) : CommandLineInfo {
    override val dialogTitle: String = "Tasks and Arguments"
    override val dialogTooltip: String = "Insert Tasks and Arguments..."

    override val fieldEmptyState: String = dialogTitle

    override val settingsHint: String = "Example: resolveInterpreter"
    override val settingsName: String = "Tasks and arguments"

    override val tablesInfo: List<CompletionTableInfo> = listOf(PaddleTasksCompletionTableInfo(project, workingDirectoryField))


    class PaddleTasksCompletionTableInfo(val project: Project, val workingDirectoryField: WorkingDirectoryField) : CompletionTableInfo {
        override val emptyState: String = "No Paddle tasks"

        override val dataColumnIcon: Icon = AllIcons.General.Gear
        override val dataColumnName: String = "Task"

        override val descriptionColumnIcon: Icon? = null
        override val descriptionColumnName: String = "Description"

        private val completionInfoProperty = AtomicLazyProperty { calculateCompletionInfo() }

        override val completionInfo by completionInfoProperty
        override val tableCompletionInfo by completionInfoProperty

        private fun calculateCompletionInfo(): List<TextCompletionInfo> {
            val rootDir = project.basePath?.let { File(it) } ?: return emptyList()
            val workDir = File(workingDirectoryField.workingDirectory)

            val paddleProjectProvider = PaddleProjectProvider.getInstance(rootDir)
            val paddleProject = paddleProjectProvider.getProject(workDir)

            return paddleProject?.tasks?.all()
                ?.map { TextCompletionInfo(it.id, it.description) }
                ?: emptyList()
        }

        init {
            workingDirectoryField.whenTextModified {
                completionInfoProperty.reset()
            }
        }
    }
}
