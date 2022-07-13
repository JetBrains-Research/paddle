package io.paddle.idea.project

import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.*
import com.intellij.openapi.externalSystem.model.task.*
import com.intellij.openapi.externalSystem.service.project.ExternalSystemProjectResolver
import com.intellij.openapi.module.ModuleTypeManager
import io.paddle.idea.PaddleManager
import io.paddle.idea.settings.PaddleExecutionSettings
import io.paddle.idea.utils.IDEACommandOutput
import io.paddle.plugin.python.extensions.authConfig
import io.paddle.plugin.python.extensions.environment
import io.paddle.plugin.python.hasPython
import io.paddle.plugin.python.tasks.resolve.ResolveRequirementsTask
import io.paddle.plugin.python.utils.PaddleLogger
import io.paddle.plugin.python.utils.deepResolve
import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.PaddleProject
import io.paddle.project.PaddleProjectProvider
import io.paddle.project.extensions.descriptor
import io.paddle.project.extensions.route
import io.paddle.terminal.Terminal
import java.io.File
import java.io.FileFilter

class PaddleProjectResolver : ExternalSystemProjectResolver<PaddleExecutionSettings> {
    override fun resolveProjectInfo(
        id: ExternalSystemTaskId,
        projectPath: String,
        isPreviewMode: Boolean,
        settings: PaddleExecutionSettings?,
        listener: ExternalSystemTaskNotificationListener
    ): DataNode<ProjectData> {
        val rootDir = File(projectPath)

        // First initialization of Paddle Project
        val paddleProjectProvider = PaddleProjectProvider.getInstance(rootDir).also { it.sync() }
        val project = paddleProjectProvider.getProject(rootDir)
            ?: throw IllegalStateException("Failed to initialize Paddle project from ${rootDir.canonicalPath}")

        // Resolve requirements, interpreter, repositories (== load model to RAM)
        project.output = IDEACommandOutput(id, listener)
        PaddleLogger.terminal = Terminal(project.output)
        ResolveRequirementsTask(project).run()

        val projectData = ProjectData(
            /* owner = */ PaddleManager.ID,
            /* externalName = */ project.descriptor.name,
            /* ideProjectFileDirectoryPath = */ rootDir.canonicalPath,
            /* linkedExternalProjectPath = */ rootDir.canonicalPath
        ).also {
            it.group = project.descriptor.name
        }

        val projectDataNode = DataNode(ProjectKeys.PROJECT, projectData, null)
        val moduleByProject = createModuleNodes(projectDataNode, project.rootDir, paddleProjectProvider)
        createModuleDependencies(project, moduleByProject)

        return projectDataNode
    }

    private fun createModuleNodes(
        projectDataNode: DataNode<ProjectData>,
        currentWorkDir: File,
        provider: PaddleProjectProvider
    ): Map<PaddleProject, DataNode<ModuleData>> {
        val moduleByProject = hashMapOf<PaddleProject, DataNode<ModuleData>>()
        var moduleData: ModuleData? = null

        if (currentWorkDir.resolve("paddle.yaml").exists()) {
            // Directory is a paddle project
            val subproject = provider.getProject(currentWorkDir)
                ?: throw IllegalStateException("Could not find project in ${currentWorkDir.canonicalPath}")
            moduleData = createModuleData(
                rootDir = subproject.rootDir,
                workDir = currentWorkDir,
                moduleName = subproject.descriptor.name,
                route = subproject.route
            )
            projectDataNode.createChild(ProjectKeys.MODULE, moduleData).also {
                it.attachTasks(subproject)
                it.attachContentRoots(subproject)
                moduleByProject[subproject] = it
            }
        } else if (provider.hasProjectsIn(currentWorkDir)) {
            // Directory contains paddle projects
            val route = provider.getRouteToDir(currentWorkDir)
            moduleData = createModuleData(
                rootDir = provider.rootDir,
                workDir = currentWorkDir,
                moduleName = currentWorkDir.name,
                route = route
            )
            projectDataNode.createChild(ProjectKeys.MODULE, moduleData)
        }

        moduleData?.setProperty("directoryToRunTask", currentWorkDir.canonicalPath)
        currentWorkDir.listFiles(FileFilter { it.isDirectory })?.forEach {
            moduleByProject.putAll(createModuleNodes(projectDataNode, it, provider))
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

    private fun createModuleData(rootDir: File, workDir: File, moduleName: String, route: List<String>): ModuleData {
        val moduleFileDirectory = rootDir
            .deepResolve(".idea", "modules", rootDir.name)
            .resolve(workDir.toRelativeString(rootDir))
        return ModuleData(
            /* id = */ ":" + route.joinToString(":"),
            /* owner = */ PaddleManager.ID,
            /* moduleTypeId = */ ModuleTypeManager.getInstance().defaultModuleType.id,
            /* externalName = */ moduleName,
            /* moduleFileDirectoryPath = */ moduleFileDirectory.canonicalPath,
            /* externalConfigPath = */ workDir.canonicalPath
        ).also {
            it.internalName = route.joinToString(".")
        }
    }

    private fun DataNode<*>.attachTasks(project: PaddleProject) {
        for (task in project.tasks.all()) {
            val data = TaskData(PaddleManager.ID, task.id, project.workDir.canonicalPath, null).also {
                it.group = task.group
            }
            createChild(ProjectKeys.TASK, data)
        }
    }

    private fun DataNode<*>.attachContentRoots(project: PaddleProject) {
        val rootData = ContentRootData(PaddleManager.ID, project.workDir.canonicalPath)

        rootData.storePath(ExternalSystemSourceType.SOURCE, project.roots.sources.canonicalPath)
        rootData.storePath(ExternalSystemSourceType.TEST, project.roots.tests.canonicalPath)
        rootData.storePath(ExternalSystemSourceType.RESOURCE, project.roots.resources.canonicalPath)

        rootData.storePath(ExternalSystemSourceType.EXCLUDED, project.roots.dist.canonicalPath)
        rootData.storePath(ExternalSystemSourceType.EXCLUDED, project.workDir.resolve(".paddle").canonicalPath)
        if (project.hasPython) {
            rootData.storePath(ExternalSystemSourceType.EXCLUDED, project.environment.venv.canonicalPath)
            rootData.storePath(ExternalSystemSourceType.EXCLUDED, project.authConfig.file.canonicalPath)
        }

        createChild(ProjectKeys.CONTENT_ROOT, rootData)
    }

    override fun cancelTask(taskId: ExternalSystemTaskId, listener: ExternalSystemTaskNotificationListener): Boolean = false
}
