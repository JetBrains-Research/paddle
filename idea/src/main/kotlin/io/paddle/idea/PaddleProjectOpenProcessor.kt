package io.paddle.idea

import com.intellij.ide.actions.OpenFileAction
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.PlatformProjectOpenProcessor
import com.intellij.projectImport.ProjectOpenProcessor
import io.paddle.idea.utils.isPaddle


class PaddleProjectOpenProcessor : ProjectOpenProcessor() {
    override fun canOpenProject(file: VirtualFile): Boolean {
        if (file.isPaddle) return true
        if (file.isDirectory && file.children.any { it.isPaddle }) {
            return true
        }

        return false
    }

    override fun isProjectFile(file: VirtualFile): Boolean {
        return canOpenProject(file)
    }

    @Messages.YesNoCancelResult
    override fun askConfirmationForOpeningProject(file: VirtualFile, project: Project?): Int = Messages.NO

    override fun getName(): String = "Paddle"

    override fun doOpenProject(virtualFile: VirtualFile, projectToClose: Project?, forceOpenInNewFrame: Boolean): Project? {
        val directory = getProjectDirectory(virtualFile)
        val config = directory.findChild("paddle.yaml") ?: return run {
            LOG.warn("Can't find paddle.yaml in project folder")
            null
        }

        val project = PlatformProjectOpenProcessor.getInstance().doOpenProject(directory, projectToClose, forceOpenInNewFrame)
            ?: return null
        invokeLater { OpenFileAction.openFile(virtualFile, project) }
        ExternalSystemUtil.linkExternalProject(
            PADDLE_ID,
            PaddleExternalProjectSettings(config.canonicalPath!!),
            project,
            { if (!it) LOG.warn("Import of Paddle project has failed.") },
            false,
            ProgressExecutionMode.IN_BACKGROUND_ASYNC
        )
        return project
    }

    private fun getProjectDirectory(file: VirtualFile): VirtualFile {
        if (file.isPaddle) return file.parent
        if (file.isDirectory && file.children.any { it.isPaddle }) return file

        error("Unexpected file considered a Paddle project root")
    }

    companion object {
        private val LOG = Logger.getInstance(PaddleProjectOpenProcessor::class.java)
    }
}
