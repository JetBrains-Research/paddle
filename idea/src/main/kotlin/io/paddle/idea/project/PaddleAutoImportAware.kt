package io.paddle.idea.project

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.ExternalSystemAutoImportAware
import com.intellij.openapi.externalSystem.util.ExternalSystemApiUtil
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.util.SmartList
import io.paddle.idea.PaddleManager
import io.paddle.idea.settings.PaddleSettings
import io.paddle.plugin.python.extensions.AuthConfig
import io.paddle.utils.isPaddle
import java.io.File
import java.io.IOException
import java.nio.file.InvalidPathException

class PaddleAutoImportAware : ExternalSystemAutoImportAware {
    private val LOG: Logger = Logger.getInstance(PaddleAutoImportAware::class.java)

    override fun getAffectedExternalProjectPath(changedFileOrDirPath: String, project: Project): String? {
        if (!changedFileOrDirPath.endsWith("paddle.yaml") && !changedFileOrDirPath.endsWith(AuthConfig.FILENAME)) {
            return null
        }

        val manager = ExternalSystemApiUtil.getManager(PaddleManager.ID) ?: return null
        val systemSettings = manager.settingsProvider.`fun`(project)
        val projectsSettings = systemSettings.linkedProjectsSettings
        if (projectsSettings.isEmpty()) {
            return null
        }

        val rootPaths = HashMap<String, String>()
        for (setting in projectsSettings) {
            setting ?: continue
            for (path in setting.modules) {
                rootPaths[File(path).path] = setting.externalProjectPath
            }
        }

        var currentFile = File(changedFileOrDirPath).parentFile
        while (currentFile != null) {
            val dirPath = currentFile.path
            if (rootPaths.containsKey(dirPath)) {
                return rootPaths[dirPath]
            }
            currentFile = currentFile.parentFile
        }

        return null
    }

    override fun getAffectedExternalProjectFiles(projectPath: String?, project: Project): MutableList<File> {
        projectPath ?: return mutableListOf()

        val projectSettings = PaddleSettings.getInstance(project).getLinkedProjectSettings(projectPath)
        val subProjectPaths =
            if (projectSettings != null && FileUtil.pathsEqual(projectSettings.externalProjectPath, projectPath))
                projectSettings.modules
            else
                setOf(projectPath)
        val files = SmartList<File>()

        for (path in subProjectPaths) {
            ProgressManager.checkCanceled()
            try {
                File(path).walkTopDown()
                    .filter { it.isFile && (it.isPaddle or it.name.endsWith(AuthConfig.FILENAME)) }
                    .forEach { files.add(it) }
            } catch (e: IOException) {
                LOG.debug(e)
            } catch (e: InvalidPathException) {
                LOG.debug(e)
            }
        }

        return files
    }
}
