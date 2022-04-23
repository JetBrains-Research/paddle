package io.paddle.plugin.pyinjector.extensions

import io.paddle.plugin.interop.*
import io.paddle.project.Project
import io.paddle.utils.ext.Extendable
import kotlinx.coroutines.runBlocking

val Project.pyPluginsClient: PyPluginsClient
    get() = extensions.get(PyPluginsClient.Extension.key)!!

class PyPluginsClient(val project: Project) {

    private val client = PluginsGrpcKt.PluginsCoroutineStub(project.channel)

    object Extension : Project.Extension<PyPluginsClient> {
        override val key: Extendable.Key<PyPluginsClient> = Extendable.Key()

        override fun create(project: Project): PyPluginsClient {
            return PyPluginsClient(project)
        }
    }

    fun initializeProject() = runBlocking {
        client.initializeProjectStub(
            initializeProjectRequest {
                this.projectId = project.id
                this.workingDir = project.workDir.absolutePath
                this.pluginsSitePackages = project.pyPluginsEnvironment.venv.sitePackages.absolutePath
            }
        )
    }

    fun exportPlugins() = runBlocking {
        val pyPluginsData = project.extensions.get(PyPluginsData.Extension.key)!!

        client.exportPyPackagePlugins(
            exportPyPackagePluginsRequest {
                this.projectId = project.id
                this.plugins.addAll(pyPluginsData.pyPackages.map {
                    pyPackagePluginInfo {
                        this.pluginName = it.name
                        this.pluginVersion = it.version
                    }
                })
            }
        )

        client.exportPyModulePlugins(
            exportPyModulePluginsRequest {
                this.projectId = project.id
                this.plugins.addAll(pyPluginsData.pyModules.map {
                    pyModulePluginInfo {
                        this.pluginName = it.name
                        this.absoluteModulePath = it.absolutePathTo.toString()
                    }
                })
            }
        )
    }

    suspend fun configure(pluginName: String) {
        client.configure(
            processPluginRequest {
                this.projectId = project.id
                this.pluginName = pluginName
            }
        )
    }

    suspend fun tasks(pluginName: String): List<TaskInfo> {
        return client.tasks(
            processPluginRequest {
                this.projectId = project.id
                this.pluginName = pluginName
            }
        ).tasksInfoList
    }

    suspend fun initialize(taskId: String) {
        client.initialize(
            processTaskRequest {
                this.projectId = project.id
                this.taskId = taskId
            }
        )
    }

    suspend fun run(taskId: String) {
        client.run(
            processTaskRequest {
                this.projectId = project.id
                this.taskId = taskId
            }
        )
    }
}
