package io.paddle.plugin.remote

import java.net.Socket

object RemotePluginsClient {
    private const val host = "localhost"
    private const val port = 1234

    private val socket = Socket(host, port)

    fun getPluginIds(): List<String> {
        BasePluginRepoRequest.newBuilder()
            .setPluginsRequest(GetPluginIdsRequest.newBuilder().build())
            .build()
            .writeDelimitedTo(socket.getOutputStream())
        val response = BasePluginRepoResponse.parseDelimitedFrom(socket.getInputStream())
        return if (response.hasPluginsResponse()) {
            response.pluginsResponse.pluginIdsList
        } else emptyList()
    }

    fun getTasksBy(pluginId: String): List<TaskInfo> {
        BasePluginRepoRequest.newBuilder()
            .setTasksRequest(GetTasksRequest.newBuilder().setPluginId(pluginId).build())
            .build()
            .writeDelimitedTo(socket.getOutputStream())
        val response = BasePluginRepoResponse.parseDelimitedFrom(socket.getInputStream())
        return if (response.hasTasksResponse()) {
            response.tasksResponse.tasksList
        } else emptyList()
    }

    fun runTaskBy(taskId: String): String {
        BasePluginRepoRequest.newBuilder()
            .setRunTaskRequest(RunTaskRequest.newBuilder().setTaskId(taskId).build())
            .build()
            .writeDelimitedTo(socket.getOutputStream())
        val response = BasePluginRepoResponse.parseDelimitedFrom(socket.getInputStream())
        return if (response.hasRunTaskResponse()) {
            response.runTaskResponse.message
        } else "failed"
    }
}
