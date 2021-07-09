package io.paddle.idea.project

import com.intellij.openapi.Disposable
import com.intellij.openapi.externalSystem.autolink.ExternalSystemProjectLinkListener
import com.intellij.openapi.externalSystem.autolink.ExternalSystemUnlinkedProjectAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import io.paddle.idea.PaddleExternalSystemManager
import io.paddle.idea.project.open.PaddleOpenProjectProvider
import io.paddle.idea.settings.*
import io.paddle.idea.utils.isPaddle

class PaddleUnlinkedProjectAware : ExternalSystemUnlinkedProjectAware {
    override val systemId = PaddleExternalSystemManager.ID

    override fun isBuildFile(project: Project, buildFile: VirtualFile): Boolean {
        return buildFile.isPaddle
    }

    override fun isLinkedProject(project: Project, externalProjectPath: String): Boolean {
        return project.getUserData(PaddleExternalProjectSettings.KEY) != null
    }

    @Suppress("UnstableApiUsage")
    override fun subscribe(project: Project, listener: ExternalSystemProjectLinkListener, parentDisposable: Disposable) {
        val settings = project.getUserData(PaddleExternalSystemSettings.KEY)!!
        settings.subscribe(object : PaddleExternalProjectSettingsListener.Adapter() {
            override fun onProjectsLinked(settings: Collection<PaddleExternalProjectSettings>) {
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
