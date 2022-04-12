package io.paddle.idea.startup

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.externalSystem.ExternalSystemManager
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataImportListener
import com.intellij.openapi.externalSystem.settings.ExternalProjectSettings
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListenerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.jetbrains.python.sdk.basePath
import com.jetbrains.python.statistics.modules
import io.paddle.idea.PaddleManager
import io.paddle.idea.sdk.PaddlePythonSdkUtil
import io.paddle.plugin.python.extensions.environment
import io.paddle.plugin.python.hasPython
import io.paddle.project.PaddleProjectProvider
import java.io.File

class PaddleProjectSettingsUpdater : ExternalSystemSettingsListenerEx {
    override fun onProjectsLinked(
        project: Project,
        manager: ExternalSystemManager<*, *, *, *, *>,
        settings: Collection<ExternalProjectSettings>
    ) {
        if (ApplicationManager.getApplication().isUnitTestMode) return
        if (manager !is PaddleManager) return

        val connection = project.messageBus.connect(Disposer.newDisposable())
        connection.subscribe(ProjectDataImportListener.TOPIC, ProjectDataImportListener {
            val rootDir = project.basePath?.let { File(it) } ?: return@ProjectDataImportListener

            val projectProvider = PaddleProjectProvider.getInstance(rootDir)
            if (!projectProvider.hasProjectsIn(rootDir)) {
                projectProvider.initializeProject()
            }

            for (module in project.modules) {
                val subproject = module.basePath?.let { projectProvider.findBy(File(it)) } ?: continue
                if (subproject.hasPython && subproject.environment.venv.exists()) {
                    PaddlePythonSdkUtil.configurePythonSdk(module, subproject)
                }
            }
        })
    }
}
