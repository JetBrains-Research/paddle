#!/usr/bin/env python3

from concurrent import futures
from dataclasses import dataclass

import click
import grpc

import plugins_pb2 as plugins_api
import plugins_pb2_grpc as grpc_servicer
from src.client import PaddleClientConfiguration
from src.container import PluginsContainer


@dataclass
class PaddlePluginsServiceConfiguration:
    working_dir: str
    n_containers: int = 1
    client_config: PaddleClientConfiguration = PaddleClientConfiguration()


class PaddlePluginsService(grpc_servicer.PluginsServicer):
    def __init__(self, config: PaddlePluginsServiceConfiguration) -> None:
        self.__containers = [PluginsContainer(config.working_dir, config.client_config) for _ in range(config.n_containers)]

    def GetTasks(self, request, context):
        container = self.__containers[0]
        tasks = container.get_tasks(request.pluginId)
        # todo: remove redundant list creation
        tasks_info = list(map(lambda task: plugins_api.TaskInfo(id=task.name, group=task.group, deps=task.deps), tasks))
        return plugins_api.GetTasksResponse(taskInfoList=tasks_info)

    def Run(self, request, context):
        container = self.__containers[0]
        # todo: replace mock id '1' with actual one
        container.execute_act("1", request.pluginId, request.taskId)
        return plugins_api.google_dot_protobuf_dot_empty__pb2.Empty()

    def start(self) -> None:
        for container in self.__containers:
            container.start()

    def stop(self) -> None:
        for container in self.__containers:
            container.stop()


@click.command()
@click.option("--server_port", default=50052, help="Port number to Python plugins server start")
@click.option("--client_port", default=50051, help="Port number to client connect to Paddle build system")
@click.option("--working_dir", default=".", help="Working directory")
def main(server_port: int, client_port: int, working_dir: str) -> None:
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    service = PaddlePluginsService(PaddlePluginsServiceConfiguration(working_dir=working_dir,
                                                                     client_config=PaddleClientConfiguration(port=client_port)))
    grpc_servicer.add_PluginsServicer_to_server(service, server)
    server.add_insecure_port(f"[::]:{server_port}")
    service.start()
    server.start()
    server.wait_for_termination()
    service.stop()


if __name__ == '__main__':
    main()
