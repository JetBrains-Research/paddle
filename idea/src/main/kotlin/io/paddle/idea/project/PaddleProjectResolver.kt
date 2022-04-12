package io.paddle.idea.project

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.*
import com.intellij.openapi.externalSystem.model.task.*
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.openapi.module.ModuleTypeManager
import io.paddle.idea.PaddleManager
import io.paddle.idea.settings.PaddleExecutionSettings
import io.paddle.plugin.python.extensions.environment
import io.paddle.plugin.python.hasPython
import io.paddle.plugin.standard.extensions.*
import io.paddle.project.PaddleProject
import io.paddle.project.PaddleProjectProvider
import java.io.File

class PaddleProjectResolver : ExternalSystemProjectResolver<PaddleExecutionSettings> {
    override fun resolveProjectInfo(
        id: ExternalSystemTaskId,
        projectPath: String,
        isPreviewMode: Boolean,
        settings: PaddleExecutionSettings?,
        listener: ExternalSystemTaskNotificationListener
    ): DataNode<ProjectData> {
        val pathToProject = File(projectPath).parentFile
        val projectProvider = PaddleProjectProvider.getInstance(rootDir = pathToProject)
        val project = projectProvider.initializeProject() // first initialization

        val projectData = ProjectData(
            PaddleManager.ID,
            project.descriptor.name,
            pathToProject.canonicalPath,
            pathToProject.canonicalPath
        ).also {
            it.group = project.descriptor.name
            it.version = project.descriptor.version
        }

        val projectNode = DataNode(ProjectKeys.PROJECT, projectData, null)
        val rootModuleNode = projectNode.createChild(ProjectKeys.MODULE, project.getModuleData()).apply {
            attachTasks(project)
            attachContentRoots(project)
        }
        createModuleNodes(project.workDir, rootModuleNode, projectProvider).also {
            createModuleDependencies(project, it + (project to rootModuleNode))
        }

        return projectNode
    }

    private fun createModuleNodes(
        workDir: File,
        moduleNode: DataNode<ModuleData>,
        projectProvider: PaddleProjectProvider
    ): Map<PaddleProject, DataNode<ModuleData>> {
        val moduleByProject = hashMapOf<PaddleProject, DataNode<ModuleData>>()
        workDir.listFiles()?.filter { it.isDirectory }?.forEach { dir ->
            val childModuleNode: DataNode<ModuleData>
            if (dir.resolve("paddle.yaml").exists()) {
                // Directory is a paddle project
                val subproject = projectProvider.findBy(dir) ?: throw IllegalStateException("Projects were not initialized completely.")
                childModuleNode = moduleNode.createChild(ProjectKeys.MODULE, subproject.getModuleData()).also {
                    it.attachTasks(subproject)
                    it.attachContentRoots(subproject)
                    moduleByProject[subproject] = it
                }
                moduleByProject.putAll(createModuleNodes(dir, childModuleNode, projectProvider))
            } else if (projectProvider.hasProjectsIn(dir)) {
                // Directory contains paddle projects
                childModuleNode = moduleNode.createChild(
                    ProjectKeys.MODULE,
                    ModuleData(
                        dir.canonicalPath,
                        PaddleManager.ID,
                        ModuleTypeManager.getInstance().defaultModuleType.id,
                        dir.name,
                        dir.canonicalPath,
                        dir.canonicalPath
                    )
                )
                moduleByProject.putAll(createModuleNodes(dir, childModuleNode, projectProvider))
            }
        }
        return moduleByProject
    }

    private fun createModuleDependencies(project: PaddleProject, moduleByProject: Map<PaddleProject, DataNode<ModuleData>>) {
        for (subproject in project.subprojects) {
            val ownerNode = moduleByProject[project]
                ?: throw IllegalStateException("Can't find corresponding module for owner paddle project :${project.descriptor.name}")
            val depNode = moduleByProject[subproject]
                ?: throw IllegalStateException("Can't find corresponding module for dependent paddle project :${subproject.descriptor.name}")
            ownerNode.createChild(ProjectKeys.MODULE_DEPENDENCY, ModuleDependencyData(ownerNode.data, depNode.data))
            createModuleDependencies(subproject, moduleByProject)
        }
    }

    private fun PaddleProject.getModuleData(): ModuleData {
        return ModuleData(
            this.id,
            PaddleManager.ID,
            ModuleTypeManager.getInstance().defaultModuleType.id,
            this.descriptor.name,
            this.workDir.canonicalPath,
            this.buildFile.canonicalPath
        )
    }


    private fun DataNode<ModuleData>.attachTasks(project: PaddleProject) {
        for (task in project.tasks.all()) {
            val data = TaskData(PaddleManager.ID, task.id, project.workDir.canonicalPath, null).also {
                it.group = task.group
            }
            createChild(ProjectKeys.TASK, data)
        }
    }

    private fun DataNode<ModuleData>.attachContentRoots(project: PaddleProject) {
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
