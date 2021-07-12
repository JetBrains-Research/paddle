package io.paddle.idea.project

import com.intellij.openapi.Disposable
import com.intellij.openapi.externalSystem.autolink.ExternalSystemProjectLinkListener
import com.intellij.openapi.externalSystem.autolink.ExternalSystemUnlinkedProjectAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import io.paddle.idea.PaddleManager
import io.paddle.idea.project.open.PaddleOpenProjectProvider
import io.paddle.idea.settings.*
import io.paddle.idea.utils.isPaddle

class PaddleUnlinkedProjectAware : ExternalSystemUnlinkedProjectAware {
    override val systemId = PaddleManager.ID

    override fun isBuildFile(project: Project, buildFile: VirtualFile): Boolean {
        return buildFile.isPaddle
    }

    override fun isLinkedProject(project: Project, externalProjectPath: String): Boolean {
        return PaddleSettings.getInstance(project).getLinkedProjectSettings(externalProjectPath) != null
    }

    @Suppress("UnstableApiUsage")
    override fun subscribe(project: Project, listener: ExternalSystemProjectLinkListener, parentDisposable: Disposable) {
        val settings = project.getUserData(PaddleSettings.KEY)!!
        settings.subscribe(object : PaddleProjectSettings.Listener.Adapter() {
            override fun onProjectsLinked(settings: Collection<PaddleProjectSettings>) {
                settings.forEach { listener.onProjectLinked(it.externalProjectPath) }
            }

            override fun onProjectsUnlinked(linkedProjectPaths: Set<String>) {
                linkedProjectPaths.forEach { listener.onProjectUnlinked(it) }
            }
        }, parentDisposable)
    }

    override fun linkAndLoadProject(project: Project, externalProjectPath: String) {
        PaddleOpenProjectProvider.linkToExistingProject(externalProjectPath, project)
    }
}
