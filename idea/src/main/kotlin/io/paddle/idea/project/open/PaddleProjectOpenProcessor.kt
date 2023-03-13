package io.paddle.idea.project.open

import com.intellij.ide.impl.runUnderModalProgressIfIsEdt
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.projectImport.ProjectOpenProcessor


class PaddleProjectOpenProcessor(override val name: String = "Paddle") : ProjectOpenProcessor() {
    override fun canOpenProject(file: VirtualFile): Boolean = PaddleOpenProjectProvider.canOpenProject(file)

    override fun doOpenProject(virtualFile: VirtualFile, projectToClose: Project?, forceOpenInNewFrame: Boolean): Project? {
        return runUnderModalProgressIfIsEdt {
            PaddleOpenProjectProvider.openProject(virtualFile, projectToClose, forceOpenInNewFrame)
        }
    }

    override fun canImportProjectAfterwards(): Boolean {
        return true
    }

    override fun importProjectAfterwards(project: Project, file: VirtualFile) {
        PaddleOpenProjectProvider.linkToExistingProject(file, project)
    }
}
