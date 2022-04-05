package io.paddle.idea.project

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.*
import com.intellij.openapi.externalSystem.model.task.*
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.openapi.module.ModuleTypeManager
import io.paddle.idea.PaddleManager
import io.paddle.idea.settings.PaddleExecutionSettings
import io.paddle.idea.utils.PaddleProject
import io.paddle.plugin.python.extensions.environment
import io.paddle.plugin.python.hasPython
import io.paddle.plugin.standard.extensions.*
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
        val project = PaddleProject.load(pathToProjectFile, pathToProject) // fixme

        val projectData = ProjectData(
            PaddleManager.ID,
            project.descriptor.name,
            pathToProject.canonicalPath,
            pathToProject.canonicalPath
        ).also {
            it.group = project.descriptor.name
            it.version = project.descriptor.version
        }

        val projectNode = DataNode(ProjectKeys.PROJECT, projectData, null).also {
            traverseProjectModules(project, project.createModuleData(), it)
        }

        return projectNode
    }

    private fun traverseProjectModules(project: Project, data: ModuleData, parentDataNode: DataNode<*>) {
        val moduleDataNode = parentDataNode.createChild(ProjectKeys.MODULE, data).also {
            it.attachTasks(project)
            it.attachContentRoots(project)
        }
        for (subproject in project.subprojects) {
            val submoduleData = subproject.createModuleData()
            val moduleDependencyData = ModuleDependencyData(data, submoduleData)
            moduleDataNode.createChild(ProjectKeys.MODULE_DEPENDENCY, moduleDependencyData)
            traverseProjectModules(subproject, submoduleData, moduleDataNode)
        }
    }

    private fun Project.createModuleData() = ModuleData(
        id,
        PaddleManager.ID,
        ModuleTypeManager.getInstance().defaultModuleType.id,
        descriptor.name,
        workDir.canonicalPath,
        buildFile.canonicalPath
    )


    private fun DataNode<ModuleData>.attachTasks(project: Project) {
        for (task in project.tasks.all()) {
            val data = TaskData(PaddleManager.ID, task.id, project.workDir.canonicalPath, null).also {
                it.group = task.group
            }
            createChild(ProjectKeys.TASK, data)
        }
    }

    private fun DataNode<ModuleData>.attachContentRoots(project: Project) {
        val rootData = ContentRootData(PaddleManager.ID, project.workDir.canonicalPath)
        for (src in project.roots.sources) {
            rootData.storePath(ExternalSystemSourceType.SOURCE, src.canonicalPath)
        }
        for (tests in project.roots.tests) {
            rootData.storePath(ExternalSystemSourceType.TEST, tests.canonicalPath)
        }
        for (resources in project.roots.resources) {
            rootData.storePath(ExternalSystemSourceType.RESOURCE, resources.canonicalPath)
        }

        rootData.storePath(ExternalSystemSourceType.EXCLUDED, project.workDir.resolve(".paddle").canonicalPath)
        if (project.hasPython) {
            rootData.storePath(ExternalSystemSourceType.EXCLUDED, project.environment.venv.canonicalPath)
        }

        createChild(ProjectKeys.CONTENT_ROOT, rootData)
    }

    override fun cancelTask(taskId: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean = false
}
