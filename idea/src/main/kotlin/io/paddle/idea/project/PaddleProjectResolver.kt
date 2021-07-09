package io.paddle.idea.project

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.*
import com.intellij.openapi.externalSystem.model.task.*
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.openapi.module.ModuleTypeManager
import io.paddle.idea.*
import io.paddle.idea.settings.PaddleExecutionSettings
import io.paddle.idea.utils.PaddleProject
import io.paddle.plugin.standard.extensions.descriptor
import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.Project
import java.io.File

class PaddleProjectResolver : ExternalSystemProjectResolver<PaddleExecutionSettings> {
    override fun resolveProjectInfo(
        id: ExternalSystemTaskId,
        projectPath: String,
        isPreviewMode: Boolean,
        settings: PaddleExecutionSettings?,
        listener: ExternalSystemTaskNotificationListener
    ): DataNode<ProjectData> {
        val pathToProjectFile = File(projectPath)
        val pathToProject = pathToProjectFile.parentFile
        val project = PaddleProject.load(pathToProjectFile, pathToProject)

        val projectData = ProjectData(PaddleExternalSystemManager.ID, project.descriptor.name, pathToProject.canonicalPath, pathToProject.canonicalPath).also {
            it.group = project.descriptor.name
            it.version = project.descriptor.version
        }

        val projectNode = DataNode(ProjectKeys.PROJECT, projectData, null)

        val moduleData = ModuleData(
            "main",
            PaddleExternalSystemManager.ID,
            ModuleTypeManager.getInstance().defaultModuleType.id,
            project.descriptor.name,
            pathToProject.canonicalPath, pathToProject.canonicalPath
        )

        val moduleNode = projectNode.createChild(ProjectKeys.MODULE, moduleData)

        moduleNode.also {
            it.attachContentRoot(pathToProject, project)
            it.attachTasks(pathToProject, project)
        }

        return projectNode
    }

    private fun DataNode<ModuleData>.attachTasks(
        pathToProject: File,
        project: Project
    ) {
        for (task in project.tasks.all()) {
            val data = TaskData(PaddleExternalSystemManager.ID, task.id, pathToProject.canonicalPath, null).also {
                it.group = task.group
            }
            createChild(ProjectKeys.TASK, data)
        }
    }

    private fun DataNode<ModuleData>.attachContentRoot(pathToProject: File, project: Project) {
        val rootData = ContentRootData(PaddleExternalSystemManager.ID, pathToProject.canonicalPath)
        for (src in project.roots.sources) {
            rootData.storePath(ExternalSystemSourceType.SOURCE, src.canonicalPath)
        }
        for (tests in project.roots.tests) {
            rootData.storePath(ExternalSystemSourceType.TEST, tests.canonicalPath)
        }
        for (resources in project.roots.resources) {
            rootData.storePath(ExternalSystemSourceType.RESOURCE, resources.canonicalPath)
        }
        createChild(ProjectKeys.CONTENT_ROOT, rootData)
    }

    override fun cancelTask(taskId: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean = false
}
