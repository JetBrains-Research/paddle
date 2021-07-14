package io.paddle.idea.project.open

import com.intellij.ide.actions.OpenFileAction
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.platform.PlatformProjectOpenProcessor
import com.intellij.projectImport.ProjectOpenProcessor
import io.paddle.idea.utils.isPaddle


class PaddleProjectOpenProcessor : ProjectOpenProcessor() {
    override fun getName(): String = "Paddle"

    override fun canOpenProject(file: VirtualFile): Boolean = PaddleOpenProjectProvider.canOpenProject(file)

    override fun doOpenProject(virtualFile: VirtualFile, projectToClose: Project?, forceOpenInNewFrame: Boolean): Project? {
        return PaddleOpenProjectProvider.openProject(virtualFile, projectToClose, forceOpenInNewFrame)
    }

    override fun canImportProjectAfterwards(): Boolean {
        return true
    }

    override fun importProjectAfterwards(project: Project, file: VirtualFile) {
        PaddleOpenProjectProvider.linkToExistingProject(file, project)
    }
}
