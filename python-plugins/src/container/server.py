#!/usr/bin/env python3

from concurrent import futures

import grpc

import project_pb2 as project_api
import plugins_pb2_grpc as grpc_servicer
import project_pb2_grpc as project_servicer
import plugins_pb2 as plugins_api


class PaddlePlugins(grpc_servicer.PluginsServicer):
    def GetTasks(self, request, context):
        greeting_info = plugins_api.TaskInfo(id="greeting", group="application", deps=[])
        tasks = [greeting_info] if request.pluginId == "greeting" else []
        return plugins_api.GetTasksResponse(taskInfoList=tasks)

    def Run(self, request, context):
        with grpc.insecure_channel('localhost:50051') as channel:
            stub = project_servicer.ProjectStub(channel)
            if request.pluginId == "greeting" and request.taskId == "greeting":
                stub.PrintMessage(project_api.PrintRequest(projectId="1", message="Hello, world!", isErr=False, color="GREEN"))
            else:
                stub.PrintMessage(project_api.PrintRequest(projectId="1", message="PADDLE TASK WARNING!", isErr=False, color="RED"))

        return plugins_api.google_dot_protobuf_dot_empty__pb2.Empty()


def serve():
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    grpc_servicer.add_PluginsServicer_to_server(PaddlePlugins(), server)
    server.add_insecure_port('[::]:50052')
    server.start()
    server.wait_for_termination()


if __name__ == '__main__':
    serve()
