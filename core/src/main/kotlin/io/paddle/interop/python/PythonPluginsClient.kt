package io.paddle.interop.python

import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.paddle.plugin.interop.*
import io.paddle.plugin.interop.PluginsGrpcKt.PluginsCoroutineStub
import java.io.Closeable
import java.util.concurrent.TimeUnit

object PythonPluginsClient : Closeable {
    private const val port = 50052

    private val channel: ManagedChannel = ManagedChannelBuilder
        .forAddress("localhost", port)
        .usePlaintext()
        .build()

    private val client = PluginsCoroutineStub(channel)

    suspend fun getInfoAboutAvailableTasksFor(pluginId: String): List<TaskInfo> {
        val request = getTasksRequest {
            this.pluginId = pluginId
        }
        return client.getTasks(request).taskInfoListList ?: emptyList()
    }

    suspend fun runTaskWith(pluginId: String, taskId: String) {
        val request = taskProcessRequest {
            this.pluginId = pluginId
            this.taskId = taskId
        }
        client.run(request)
    }

    override fun close() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
}
