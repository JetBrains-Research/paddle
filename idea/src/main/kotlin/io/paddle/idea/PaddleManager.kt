package io.paddle.idea

import com.intellij.execution.configurations.SimpleJavaParameters
import com.intellij.openapi.externalSystem.*
import com.intellij.openapi.externalSystem.model.ProjectSystemId
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.openapi.externalSystem.service.project.autoimport.CachingExternalSystemAutoImportAware
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.externalSystem.util.ExternalSystemConstants
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.openapi.util.registry.Registry
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.Function
import icons.ExternalSystemIcons
import icons.PythonIcons
import io.paddle.idea.execution.PaddleTaskManager
import io.paddle.idea.project.PaddleAutoImportAware
import io.paddle.idea.project.PaddleProjectResolver
import io.paddle.idea.settings.*
import io.paddle.idea.utils.isPaddle
import io.paddle.utils.config.paddleHomeEnvProvider
import org.koin.core.context.startKoin
import java.io.File
import javax.swing.Icon

class PaddleManager : ExternalSystemManager<
    PaddleProjectSettings,
    PaddleProjectSettings.Listener,
    PaddleSettings,
    PaddleLocalSettings,
    PaddleExecutionSettings
    >,
    ExternalSystemUiAware,
    ExternalSystemConfigurableAware,
    ExternalSystemAutoImportAware {

    companion object {
        val ID: ProjectSystemId = ProjectSystemId("Paddle")

        private val FILE_CHOOSER_DESCRIPTOR = object : FileChooserDescriptor(true, false, false, false, false, false) {
            override fun isFileSelectable(file: VirtualFile?) = super.isFileSelectable(file) && file != null && file.isPaddle
        }
    }

    private val autoImportDelegate = CachingExternalSystemAutoImportAware(PaddleAutoImportAware())

    override fun getAffectedExternalProjectPath(changedFileOrDirPath: String, project: Project): String? {
        return autoImportDelegate.getAffectedExternalProjectPath(changedFileOrDirPath, project)
    }

    override fun getAffectedExternalProjectFiles(projectPath: String?, project: Project): MutableList<File> {
        return autoImportDelegate.getAffectedExternalProjectFiles(projectPath, project)
    }

    init {
        @Suppress("UnresolvedPluginConfigReference")
        Registry.get("${ID.id}${ExternalSystemConstants.USE_IN_PROCESS_COMMUNICATION_REGISTRY_KEY_SUFFIX}").setValue(true)
        startKoin {
            modules(paddleHomeEnvProvider)
        }
    }

    override fun enhanceRemoteProcessing(parameters: SimpleJavaParameters) {
    }

    override fun getSystemId(): ProjectSystemId = ID

    override fun getSettingsProvider(): Function<Project, PaddleSettings> {
        return Function {
            PaddleSettings.getInstance(it)
        }
    }

    override fun getLocalSettingsProvider(): Function<Project, PaddleLocalSettings> {
        return Function<Project, PaddleLocalSettings> { project: Project -> project.getService(PaddleLocalSettings::class.java) }
    }

    override fun getExecutionSettingsProvider(): Function<Pair<Project, String>, PaddleExecutionSettings> {
        return Function { pair ->
            val rootProjectPath = pair.first.basePath
            PaddleExecutionSettings(File(rootProjectPath))
        }
    }

    override fun getProjectResolverClass(): Class<out ExternalSystemProjectResolver<PaddleExecutionSettings>> {
        return PaddleProjectResolver::class.java
    }

    override fun getTaskManagerClass(): Class<out ExternalSystemTaskManager<PaddleExecutionSettings>> {
        return PaddleTaskManager::class.java
    }

    override fun getExternalProjectConfigDescriptor(): FileChooserDescriptor = FILE_CHOOSER_DESCRIPTOR

    override fun getExternalProjectDescriptor(): FileChooserDescriptor = FILE_CHOOSER_DESCRIPTOR

    override fun getProjectRepresentationName(targetProjectPath: String, rootProjectPath: String?): String {
        return ExternalSystemApiUtil.getProjectRepresentationName(targetProjectPath, rootProjectPath);
    }

    override fun getProjectIcon(): Icon = PythonIcons.Python.Python

    override fun getTaskIcon(): Icon = ExternalSystemIcons.Task

    override fun getConfigurable(project: Project): Configurable {
        return PaddleConfigurable(project)
    }
}
