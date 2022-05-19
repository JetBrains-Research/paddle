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
import io.paddle.plugin.python.extensions.environment
import io.paddle.plugin.python.hasPython
import io.paddle.plugin.python.tasks.resolve.ResolveRequirementsTask
import io.paddle.plugin.python.utils.deepResolve
import io.paddle.plugin.standard.extensions.roots
import io.paddle.project.PaddleProject
import io.paddle.project.PaddleProjectProvider
import io.paddle.project.extensions.descriptor
import java.io.File

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
        ResolveRequirementsTask(project).run()

        val projectData = ProjectData(
            /* owner = */ PaddleManager.ID,
            /* externalName = */ project.descriptor.name,
            /* ideProjectFileDirectoryPath = */ rootDir.canonicalPath,
            /* linkedExternalProjectPath = */ rootDir.canonicalPath
        ).also {
            it.group = project.descriptor.name
            it.version = project.descriptor.version
        }

        val projectDataNode = DataNode(ProjectKeys.PROJECT, projectData, null)
        val rootModuleDataNode = projectDataNode.createChild(ProjectKeys.MODULE, project.getModuleData()).apply {
            attachTasks(project)
            attachContentRoots(project)
        }

        // Create IntelliJ modules for each Paddle subproject
        val moduleByProject = createModuleNodes(project.workDir, rootModuleDataNode, paddleProjectProvider)

        // Create dependencies between IntelliJ modules according to Paddle subprojects' dependencies
        createModuleDependencies(project, moduleByProject + (project to rootModuleDataNode))

        return projectDataNode
    }

    private fun createModuleNodes(
        workDir: File,
        moduleNode: DataNode<*>,
        provider: PaddleProjectProvider
    ): Map<PaddleProject, DataNode<ModuleData>> {
        val moduleByProject = hashMapOf<PaddleProject, DataNode<ModuleData>>()
        workDir.listFiles()?.filter { it.isDirectory }?.forEach { dir ->
            val childModuleNode: DataNode<ModuleData>
            if (dir.resolve("paddle.yaml").exists()) {
                // Directory is a paddle project
                val subproject = provider.getProject(dir)
                    ?: throw IllegalStateException("Could not find project in ${dir.canonicalPath}")
                childModuleNode = moduleNode.createChild(ProjectKeys.MODULE, subproject.getModuleData()).also {
                    it.attachTasks(subproject)
                    it.attachContentRoots(subproject)
                    moduleByProject[subproject] = it
                }
                moduleByProject.putAll(createModuleNodes(dir, childModuleNode, provider))
            } else if (provider.hasProjectsIn(dir)) {
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
                moduleByProject.putAll(createModuleNodes(dir, childModuleNode, provider))
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
        val moduleFileDirectory = rootDir
            .deepResolve(".idea", "modules", rootDir.name)
            .resolve(workDir.toRelativeString(rootDir))
        return ModuleData(
            /* id = */ id,
            /* owner = */ PaddleManager.ID,
            /* moduleTypeId = */ ModuleTypeManager.getInstance().defaultModuleType.id,
            /* externalName = */ descriptor.name,
            /* moduleFileDirectoryPath = */ moduleFileDirectory.canonicalPath,
            /* externalConfigPath = */ workDir.canonicalPath
        )
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
