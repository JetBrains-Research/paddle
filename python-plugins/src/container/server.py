#!/usr/bin/env python3
import importlib
import os
import sys
from concurrent import futures
from dataclasses import dataclass
from typing import List, Dict

import click
import grpc

import plugins_pb2 as plugins_api
import plugins_pb2_grpc as grpc_servicer
import project_pb2 as project_api
import project_pb2_grpc as project_servicer
from src.container.paddle_api import PaddlePlugin, PaddleTask, AbstractPaddleProject


class PaddlePluginsProvider:
    def __init__(self, plugins_dir: str) -> None:
        self.__plugins_dir = plugins_dir
        self.__plugins = dict()

        # todo: replace dynamically modifying sys.path with using lib/site-package inside plugins dir
        for dir_name in os.listdir(plugins_dir):
            dir_path = os.path.join(plugins_dir, dir_name)
            if os.path.isdir(dir_path):
                sys.path.append(dir_path)

    @property
    def plugins(self) -> Dict[str, PaddlePlugin]:
        return self.__plugins

    def __getitem__(self, plugin_id: str) -> PaddlePlugin:
        return self.get(plugin_id)

    def get(self, plugin_id: str) -> PaddlePlugin:
        plugin = self.plugins.get(plugin_id, None)
        if not plugin:
            mod = importlib.import_module(plugin_id)
            plugin = getattr(mod, "plugin")
            self.plugins[plugin_id] = plugin

        if not plugin:
            raise KeyError(f"cannot find plugin: {plugin_id}")

        return plugin

    def invalidate_cache(self):
        self.__plugins = dict()


@dataclass
class PaddleClientConfiguration:
    url: str = "localhost"
    port: int = 50051

    @property
    def address(self):
        return f"{self.url}:{self.port}"


class PluginsContainer:
    def __init__(self, working_dir: str, config: PaddleClientConfiguration) -> None:
        self.__paddle_client = None
        self.__plugins_provider = None
        self.__projects = dict()
        self.__paddle_client_config = config
        self.__plugins_dir = f"{working_dir}/plugins"
        self.__started = False

    def __assert_started(self) -> None:
        if not self.__started:
            raise RuntimeError("plugins container not started yet")

    def get_tasks(self, plugin_id: str) -> List[PaddleTask]:
        self.__assert_started()
        plugin = self.__plugins_provider[plugin_id]
        return list(plugin.tasks.values())

    def execute_act(self, project_id: str, plugin_id: str, task_id: str) -> None:
        self.__assert_started()
        project = self.__projects.get(project_id, None)
        if not project:
            project = GrpcStubPaddleProject(project_id, self.__paddle_client)
            self.__projects[project_id] = project
        plugin = self.__plugins_provider[plugin_id]
        plugin.task(task_id).act(project=project)

    def start(self) -> None:
        if self.__started:
            raise RuntimeError("plugins container already started")
        self.__paddle_client = PaddleClient(configuration=self.__paddle_client_config)
        self.__plugins_provider = PaddlePluginsProvider(self.__plugins_dir)
        self.__started = True

    def stop(self) -> None:
        self.__assert_started()
        self.__paddle_client.stop()
        self.__plugins_provider.invalidate_cache()
        self.__started = False


class PaddleClient:
    def __init__(self, configuration: PaddleClientConfiguration) -> None:
        self.channel = grpc.insecure_channel(configuration.address)
        self.stub = project_servicer.ProjectStub(self.channel)

    def print_message(self, project_id: str, message: str, is_err: bool, color: str) -> None:
        self.stub.PrintMessage(project_api.PrintRequest(
            projectId=project_id, message=message, isErr=is_err, color=color)
        )

    def close(self) -> None:
        self.channel.stop()


class GrpcStubPaddleProject(AbstractPaddleProject):
    def __init__(self, project_id: str, grpc_client: PaddleClient):
        self.__project_id = project_id
        self.__paddle_client = grpc_client

    def print_message(self, message: str, is_err: bool, color: str):
        self.__paddle_client.print_message(self.__project_id, message, is_err, color)


@dataclass
class PaddlePluginsServiceConfiguration:
    working_dir: str
    port: int = 50052
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


def startup_routine():
    pass


def stop_routine():
    pass


@click.command()
@click.option("--server_port", default=50052, help="Port number to Python plugins server start")
@click.option("--client_port", default=50051, help="Port number to client connect to Paddle build system")
@click.option("--working_dir", default=".", help="Working directory")
def main(server_port: int, client_port: int, working_dir: str) -> None:
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    service = PaddlePluginsService(PaddlePluginsServiceConfiguration(working_dir=working_dir))
    grpc_servicer.add_PluginsServicer_to_server(service, server)
    server.add_insecure_port('[::]:50052')
    service.start()
    server.start()
    server.wait_for_termination()
    service.stop()


if __name__ == '__main__':
    main()
