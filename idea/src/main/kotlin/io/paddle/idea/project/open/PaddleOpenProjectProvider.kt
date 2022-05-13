package io.paddle.idea.project.open

import com.intellij.openapi.externalSystem.importing.AbstractOpenProjectProvider
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.service.project.ExternalProjectRefreshCallback
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager
import com.intellij.openapi.externalSystem.service.project.manage.ExternalProjectsManagerImpl
import com.intellij.openapi.externalSystem.settings.ExternalProjectSettings
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import io.paddle.idea.PaddleManager
import io.paddle.idea.settings.PaddleProjectSettings
import io.paddle.idea.utils.isPaddle
import java.nio.file.Path
import kotlin.io.path.absolutePathString

@Suppress("UnstableApiUsage")
object PaddleOpenProjectProvider : AbstractOpenProjectProvider() {
    override fun isProjectFile(file: VirtualFile): Boolean = file.isPaddle

    override fun linkToExistingProject(projectFile: VirtualFile, project: Project) {
        val projectDirectory = if (projectFile.isDirectory) projectFile else projectFile.parent
        val projectSettings = createLinkSettings(projectDirectory.toNioPath())
        attachProjectAndRefresh(projectSettings, project)
    }

    private fun createLinkSettings(projectDirectory: Path): PaddleProjectSettings {
        val projectSettings = PaddleProjectSettings().apply {
            externalProjectPath = projectDirectory.absolutePathString()
        }
        return projectSettings
    }

    private fun attachProjectAndRefresh(settings: ExternalProjectSettings, project: Project) {
        ExternalSystemApiUtil.getSettings(project, PaddleManager.ID).linkProject(settings)

        if (Registry.`is`("external.system.auto.import.disabled")) return

        ExternalSystemUtil.refreshProject(
            settings.externalProjectPath,
            ImportSpecBuilder(project, PaddleManager.ID)
                .usePreviewMode()
                .use(ProgressExecutionMode.MODAL_SYNC)
        )

        ExternalProjectsManagerImpl.getInstance(project).runWhenInitialized {
            ExternalSystemUtil.ensureToolWindowInitialized(project, PaddleManager.ID)
            ExternalSystemUtil.refreshProject(
                settings.externalProjectPath,
                ImportSpecBuilder(project, PaddleManager.ID)
                    .callback(createFinalImportCallback(project))
            )
        }
    }


    private fun createFinalImportCallback(project: Project): ExternalProjectRefreshCallback {
        return object : ExternalProjectRefreshCallback {
            override fun onSuccess(externalProject: DataNode<ProjectData>?) {
                if (externalProject == null) return
                importData(project, externalProject)
            }
        }
    }


    private fun importData(project: Project, externalProject: DataNode<ProjectData>) {
        ProjectDataManager.getInstance().importData(externalProject, project, false)
    }
}
