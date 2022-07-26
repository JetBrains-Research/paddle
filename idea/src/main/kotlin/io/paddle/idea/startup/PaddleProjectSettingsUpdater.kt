package io.paddle.idea.startup

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.ExternalSystemManager
import com.intellij.openapi.externalSystem.service.project.manage.ProjectDataImportListener
import com.intellij.openapi.externalSystem.settings.ExternalProjectSettings
import com.intellij.openapi.externalSystem.settings.ExternalSystemSettingsListenerEx
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.LocalFileSystem
import com.jetbrains.python.sdk.*
import com.jetbrains.python.statistics.modules
import io.paddle.idea.PaddleManager
import io.paddle.idea.sdk.PaddlePythonSdkUtil
import io.paddle.plugin.python.dependencies.GlobalCacheRepository
import io.paddle.plugin.python.extensions.environment
import io.paddle.plugin.python.extensions.requirements
import io.paddle.plugin.python.hasPython
import io.paddle.project.PaddleProjectProvider
import java.io.File

class PaddleProjectSettingsUpdater : ExternalSystemSettingsListenerEx {
    private val log = Logger.getInstance(PaddleProjectSettingsUpdater::class.java)

    override fun onProjectsLinked(
        project: Project,
        manager: ExternalSystemManager<*, *, *, *, *>,
        settings: Collection<ExternalProjectSettings>
    ) {
        if (ApplicationManager.getApplication().isUnitTestMode) return
        if (manager !is PaddleManager) return

        // Schedule Python SDK configurer
        val connection = project.messageBus.connect(Disposer.newDisposable())
        connection.subscribe(ProjectDataImportListener.TOPIC, ProjectDataImportListener {
            val rootDir = project.basePath?.let { File(it) } ?: return@ProjectDataImportListener
            val provider = PaddleProjectProvider.getInstance(rootDir).also { it.sync() }

            for (module in project.modules) {
                val subproject = module.basePath?.let { provider.getProject(File(it)) } ?: continue
                if (subproject.hasPython && subproject.environment.venv.exists()) {
                    try {
                        PaddlePythonSdkUtil.configurePythonSdk(module, subproject)
                        val sdkData = module.pythonSdk?.sdkModificator?.sdkAdditionalData as? PythonSdkAdditionalData
                        for (pkg in subproject.requirements.resolved) {
                            val cachedPath = GlobalCacheRepository.getPathToCachedPackage(pkg)
                            val virtualFile = LocalFileSystem.getInstance().findFileByNioFile(cachedPath) ?: continue
                            sdkData?.addedPathFiles?.add(virtualFile)
                        }
                    } catch (e: Throwable) {
                        log.warn(e)
                    }
                }
            }
        })
    }
}
