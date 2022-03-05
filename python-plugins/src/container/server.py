#!/usr/bin/env python3

from concurrent import futures

import grpc

import plugins_pb2 as plugins_api
import plugins_pb2_grpc as grpc_servicer
import provider


class PaddlePlugins(grpc_servicer.PluginsServicer):
    def __init__(self):
        directory = "../plugins"
        self.plugin_provider = provider.PluginsProvider(directory)

    def GetTasks(self, request, context):
        plugin = self.plugin_provider.get_plugin(request.pluginId)
        tasks = list(map(lambda task: plugins_api.TaskInfo(id=task.id, group=task.group, deps=task.deps), plugin.tasks().values()))
        return plugins_api.GetTasksResponse(taskInfoList=tasks)

    def Run(self, request, context):
        plugin = self.plugin_provider.get_plugin(request.pluginId)
        plugin.task(request.taskId).act()
        return plugins_api.google_dot_protobuf_dot_empty__pb2.Empty()


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    grpc_servicer.add_PluginsServicer_to_server(PaddlePlugins(), server)
    server.add_insecure_port('[::]:50052')
    server.start()
    server.wait_for_termination()


if __name__ == '__main__':
    serve()
