package io.paddle.idea

import com.intellij.execution.configurations.SimpleJavaParameters
import com.intellij.icons.AllIcons
import com.intellij.openapi.externalSystem.ExternalSystemAutoImportAware
import com.intellij.openapi.externalSystem.ExternalSystemConfigurableAware
import com.intellij.openapi.externalSystem.ExternalSystemManager
import com.intellij.openapi.externalSystem.ExternalSystemUiAware
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager
import com.intellij.openapi.externalSystem.util.ExternalSystemConstants
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Function
import icons.ExternalSystemIcons
import io.paddle.idea.utils.isPaddle
import javax.swing.Icon

class PaddleExternalSystemManager : ExternalSystemManager<
    PaddleExternalProjectSettings,
    PaddleExternalProjectSettingsListener,
    PaddleExternalSystemSettings,
    PaddleLocalSettings,
    PaddleExecutionSettings>,
    ExternalSystemUiAware,
    ExternalSystemAutoImportAware {


    init {
        @Suppress("UnresolvedPluginConfigReference")
        val value = Registry.get("${PADDLE_ID.id}${ExternalSystemConstants.USE_IN_PROCESS_COMMUNICATION_REGISTRY_KEY_SUFFIX}")
        value.setValue(true)
    }

    override fun enhanceRemoteProcessing(parameters: SimpleJavaParameters) {
    }

    override fun getSystemId(): ProjectSystemId = PADDLE_ID

    override fun getSettingsProvider(): Function<Project, PaddleExternalSystemSettings> {
        return Function { PaddleExternalSystemSettings(it) }
    }

    override fun getLocalSettingsProvider(): Function<Project, PaddleLocalSettings> {
        return Function { PaddleLocalSettings(it) }
    }

    override fun getExecutionSettingsProvider(): Function<Pair<Project, String>, PaddleExecutionSettings> {
        return Function { PaddleExecutionSettings() }
    }

    override fun getProjectResolverClass(): Class<out ExternalSystemProjectResolver<PaddleExecutionSettings>> = PaddleProjectResolver::class.java

    override fun getTaskManagerClass(): Class<out ExternalSystemTaskManager<PaddleExecutionSettings>> {
        return PaddleTaskManager::class.java
    }

    override fun getExternalProjectConfigDescriptor(): FileChooserDescriptor = FILE_CHOOSER_DESCRIPTOR

    override fun getExternalProjectDescriptor(): FileChooserDescriptor = FILE_CHOOSER_DESCRIPTOR

    companion object {
        private val FILE_CHOOSER_DESCRIPTOR = object : FileChooserDescriptor(
            true,
            false,
            false,
            false,
            false,
            false
        ) {
            override fun isFileSelectable(file: VirtualFile?) = super.isFileSelectable(file) && file != null && file.isPaddle
        }

    }

    override fun getProjectRepresentationName(targetProjectPath: String, rootProjectPath: String?): String {
        print("WOW")
        TODO("Not yet implemented")
    }

    override fun getProjectIcon(): Icon = AllIcons.Nodes.IdeaProject

    override fun getTaskIcon(): Icon = ExternalSystemIcons.Task

    override fun getAffectedExternalProjectPath(changedFileOrDirPath: String, project: Project): String? {
        print("WOW")
        TODO("Not yet implemented")
    }
}
